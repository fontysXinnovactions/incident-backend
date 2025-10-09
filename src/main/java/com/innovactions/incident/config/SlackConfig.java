package com.innovactions.incident.config;

import com.innovactions.incident.adapter.inbound.slack.SlackCloseIncident;
import com.innovactions.incident.adapter.inbound.slack.SlackCreateIncident;
import com.innovactions.incident.adapter.inbound.slack.SlackManagerActions;
import com.innovactions.incident.adapter.inbound.slack.SlackReporterFlow;
import com.innovactions.incident.adapter.outbound.Slack.SlackBroadcaster;
import com.innovactions.incident.adapter.outbound.SlackBotMessagingAdapter;
import com.innovactions.incident.adapter.outbound.SlackChannelAdministrationAdapter;
import com.innovactions.incident.adapter.outbound.Slack.SlackIncidentClosureBroadcaster;
import com.innovactions.incident.domain.service.ChannelNameGenerator;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.IncidentClosurePort;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class SlackConfig {

    @Value("${slack.signingSecretA}")
    private String signingSecretA;

    @Value("${slack.signingSecretB}")
    private String signingSecretB;

    @Value("${slack.botTokenA}")
    private String botTokenA;

    @Value("${slack.botTokenB}")
    private String botTokenB;

    @Value("${slack.developerUserId}")
    private String developerUserId;

    /* ─────────────────────────────────────
       Reporter (Bot A)
       ───────────────────────────────────── */
    @Bean
    public App reporterSlackApp(SlackCreateIncident slackCreateIncident,
                                SlackReporterFlow slackReporterFlow) {

        var config = AppConfig.builder()
                .signingSecret(signingSecretA)
                .singleTeamBotToken(botTokenA)
                .build();

        var app = new App(config);

        app.event(com.slack.api.model.event.AppMentionEvent.class, (payload, ctx) -> {
            ctx.ack();  // ack immediately
            CompletableFuture.runAsync(() -> {
                try {
                    slackCreateIncident.handle(payload.getEvent(), ctx);
                    ctx.say("Incident noted! Thanks, we’ll look into it.");
                } catch (Exception e) {
                    // log or ignore
                }
            });
            return ctx.ack();
        });

        slackReporterFlow.register(app);
        return app;
    }

    /* ─────────────────────────────────────
       Manager (Bot B)
       ───────────────────────────────────── */
    @Bean
    public App managerSlackApp(SlackCloseIncident slackCloseIncident,
                               SlackManagerActions slackManagerActions) {

        var config = AppConfig.builder()
                .signingSecret(signingSecretB)
                .singleTeamBotToken(botTokenB)
                .build();

        var app = new App(config);

        app.command("/close_incident", (req, ctx) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    slackCloseIncident.handle(req, ctx);
                } catch (Exception e) {
                    // log or ignore
                }
            });
            return ctx.ack("Processing incident closure...");
        });

        slackManagerActions.register(app);
        return app;
    }

    /* ─────────────────────────────────────
       Servlet registrations
       ───────────────────────────────────── */
    @Bean
    public ServletRegistrationBean<Servlet> reporterServlet(App reporterSlackApp) {
        return new ServletRegistrationBean<>(new SlackAppServlet(reporterSlackApp), "/slack/reporter");
    }

    @Bean
    public ServletRegistrationBean<Servlet> managerServlet(App managerSlackApp) {
        return new ServletRegistrationBean<>(new SlackAppServlet(managerSlackApp), "/slack/manager");
    }

    /* ─────────────────────────────────────
       Adapters and ports
       ───────────────────────────────────── */
    @Bean
    public SlackCreateIncident slackCreateIncident(IncidentInboundPort incidentInboundPort) {
        return new SlackCreateIncident(incidentInboundPort);
    }

    @Bean
    public SlackCloseIncident slackCloseIncident(IncidentInboundPort incidentInboundPort) {
        return new SlackCloseIncident(incidentInboundPort);
    }

    @Bean
    public IncidentBroadcasterPort slackBroadcaster(ChannelNameGenerator channelNameGenerator) {
        return new SlackBroadcaster(botTokenB, developerUserId, channelNameGenerator);
    }

    @Bean
    public IncidentClosurePort incidentClosureBroadcaster(BotMessagingPort reporterBotMessagingPort,
                                                          BotMessagingPort managerBotMessagingPort,
                                                          ChannelAdministrationPort channelAdministrationPort) {
        return new SlackIncidentClosureBroadcaster(botTokenB, reporterBotMessagingPort,
                managerBotMessagingPort, channelAdministrationPort);
    }

    @Bean
    public SlackManagerActions slackManagerActions(ChannelAdministrationPort channelAdministrationPort,
                                                   BotMessagingPort managerBotMessagingPort) {
        return new SlackManagerActions(botTokenB, channelAdministrationPort, managerBotMessagingPort);
    }

    @Bean
    public BotMessagingPort reporterBotMessagingPort() {
        return new SlackBotMessagingAdapter(botTokenA);
    }

    @Bean
    public BotMessagingPort managerBotMessagingPort() {
        return new SlackBotMessagingAdapter(botTokenB);
    }

    @Bean
    public ChannelAdministrationPort channelAdministrationPort() {
        return new SlackChannelAdministrationAdapter(botTokenB);
    }
}
