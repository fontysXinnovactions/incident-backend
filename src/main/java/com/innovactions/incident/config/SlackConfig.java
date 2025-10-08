package com.innovactions.incident.config;

import com.innovactions.incident.adapter.inbound.slack.SlackReporterFlow;
import com.innovactions.incident.adapter.inbound.slack.SlackManagerActions;
import com.innovactions.incident.adapter.inbound.slack.SlackCloseIncident;
import com.innovactions.incident.adapter.inbound.slack.SlackCreateIncident;
import com.innovactions.incident.adapter.inbound.slack.SlackReporterFlow;
import com.innovactions.incident.adapter.outbound.SlackBroadcaster;
import com.innovactions.incident.adapter.outbound.SlackBotMessagingAdapter;
import com.innovactions.incident.adapter.outbound.SlackChannelAdministrationAdapter;
import com.innovactions.incident.adapter.outbound.SlackIncidentClosureBroadcaster;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.IncidentClosurePort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import jakarta.servlet.Servlet;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Configuration
public class SlackConfig {

  @Value("${slack.signingSecret}")
  private String signingSecret;

    @Value("${slack.signingSecretA}")
    private String signingSecretA;

    @Value("${slack.signingSecretB}")
    private String signingSecretB;

  @Bean
  public App slackApp(SlackInboundAdapter slackInboundAdapter) {
    var appConfig =
        AppConfig.builder().signingSecret(signingSecret).singleTeamBotToken(botTokenA).build();

    @Value("${slack.botTokenB}")
    private String botTokenB;

    // Reporter App (Bot A): handles user reports via @mentions
    @Bean
    public App reporterSlackApp(SlackCreateIncident slackInboundAdapter, SlackReporterFlow slackReporterFlow) {
        var appConfig = AppConfig.builder()
                .signingSecret(signingSecretA)
                .singleTeamBotToken(botTokenA)
                .build();

    app.event(
        com.slack.api.model.event.AppMentionEvent.class,
        (payload, context) -> {
          // acknowledge the event so slack doesnt retry
          var ack = context.ack();

        app.event(com.slack.api.model.event.AppMentionEvent.class, (payload, context) -> {
            var ack = context.ack();
            CompletableFuture.runAsync(() -> {
                try {
                  slackInboundAdapter.handle(payload.getEvent(), context);
                  context.say("Incident noted! Thanks, we’ll look into it.");
                } catch (Exception e) {
                  // pass
                }
            });
            return ack;
        });

        // DM/actions registration for reporter bot
        slackReporterFlow.register(app);

        return app;
    }

    // Manager App (Bot B): handles incident management for developers
    @Bean
    public App managerSlackApp(SlackCloseIncident slackCloseIncident, SlackManagerActions slackManagerActions) {
        var appConfig = AppConfig.builder()
                .signingSecret(signingSecretB)
                .singleTeamBotToken(botTokenB)
                .build();

        var app = new App(appConfig);

        app.command("/close_incident", (req, ctx) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    slackCloseIncident.handle(req, ctx);
                } catch (Exception e) {
                    // pass
                }
            });
            return ctx.ack("Processing incident closure...");
        });

        // register manager actions (ack/dismiss/leave)
        slackManagerActions.register(app);

        return app;
    }

    @Bean
    public ServletRegistrationBean<Servlet> reporterServlet(App reporterSlackApp) {
        return new ServletRegistrationBean<>(new SlackAppServlet(reporterSlackApp), "/slack/reporter");
    }

    @Bean
    public ServletRegistrationBean<Servlet> managerServlet(App managerSlackApp) {
        return new ServletRegistrationBean<>(new SlackAppServlet(managerSlackApp), "/slack/manager");
    }

    @Bean
    public SlackCreateIncident slackIncomingAdapter(IncidentInboundPort incidentInboundPort) {
        return new SlackCreateIncident(incidentInboundPort);
    }

    @Bean
    public SlackCloseIncident slackCloseIncident(IncidentInboundPort incidentInboundPort) {
        return new SlackCloseIncident(incidentInboundPort);
    }

    @Bean
    public IncidentBroadcasterPort slackBroadcaster(
            @Value("${slack.botTokenB}") String botTokenB,
            @Value("${slack.developerUserId}") String developerUserId,
            com.innovactions.incident.domain.service.ChannelNameGenerator channelNameGenerator,
            BotMessagingPort managerBotMessagingPort,
            ChannelAdministrationPort channelAdministrationPort
    ) {
        return new SlackBroadcaster(botTokenB, developerUserId, channelNameGenerator, managerBotMessagingPort, channelAdministrationPort);
    }

    @Bean
    public IncidentClosurePort incidentClosureBroadcaster(
            @Value("${slack.botTokenB}") String botTokenB,
            BotMessagingPort reporterBotMessagingPort,
            BotMessagingPort managerBotMessagingPort,
            ChannelAdministrationPort channelAdministrationPort
    ) {
        return new SlackIncidentClosureBroadcaster(botTokenB, reporterBotMessagingPort, managerBotMessagingPort, channelAdministrationPort);
    }

    @Bean
    public SlackManagerActions slackManagerActions(
            @Value("${slack.botTokenB}") String botTokenB,
            ChannelAdministrationPort channelAdministrationPort,
            BotMessagingPort managerBotMessagingPort
    ) {
        return new SlackManagerActions(botTokenB, channelAdministrationPort, managerBotMessagingPort);
    }

    @Bean
    public BotMessagingPort reporterBotMessagingPort(
            @Value("${slack.botTokenA}") String botTokenA
    ) {
        return new SlackBotMessagingAdapter(botTokenA);
    }

    @Bean
    public BotMessagingPort managerBotMessagingPort(
            @Value("${slack.botTokenB}") String botTokenB
    ) {
        return new SlackBotMessagingAdapter(botTokenB);
    }

    @Bean
    public ChannelAdministrationPort channelAdministrationPort(
            @Value("${slack.botTokenB}") String botTokenB
    ) {
        return new SlackChannelAdministrationAdapter(botTokenB);
    }

}
