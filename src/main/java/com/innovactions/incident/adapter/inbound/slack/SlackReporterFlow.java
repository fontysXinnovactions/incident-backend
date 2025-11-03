package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Handles the flow of the Reporter Bot and show actions that can be taken in Slack.</p>
 * <p>Used with the Reporter Bot in client context</p>
 */
@Component
@RequiredArgsConstructor
public class SlackReporterFlow {

    private final PendingReportState pendingReportState;
    private final IncidentInboundPort incidentInboundPort;
    private final BotMessagingPort reporterBotMessagingPort;

    public void register(App app) {
        // report_bug --> mark pending and prompt for details
        app.blockAction("report_bug", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            pendingReportState.markPending(userId);
            reporterBotMessagingPort.sendMessage(userId, "<@" + userId + ">, please describe the bug in detail.");
            return ctx.ack();
        });

        // check_status --> check status of current incident that is assigned to the user
        app.blockAction("check_status", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            reporterBotMessagingPort.sendMessage(userId, "<@" + userId + ">, status check feature is not implemented yet.");
            // TODO
            return ctx.ack();
        });

        // if pending, treat message as incident details, otherwise show actions
        app.event(MessageEvent.class, (payload, ctx) -> {
            var event = payload.getEvent();
            String channelType = event.getChannelType();

            if (event.getBotId() != null) {
                return ctx.ack();
            }
            if (!"im".equals(channelType)) {
                return ctx.ack();
            }

            String userId = event.getUser();
            String text = event.getText() == null ? "" : event.getText();

            if (pendingReportState.isPending(userId)) {
                reporterBotMessagingPort.sendMessage(userId, "Thanks! Processing your report...");
                CompletableFuture.runAsync(() -> {
                    try {
                        String reporterName = reporterBotMessagingPort.resolveUserRealName(userId);

                        CreateIncidentCommand command = new CreateIncidentCommand(
                                userId,
                                reporterName,
                                text,
                                Instant.now(),
                                Platform.SLACK
                        );
                        incidentInboundPort.reportIncident(command);
                        reporterBotMessagingPort.sendMessage(userId, "Bug assigned to an available developer. We will notify you when it's resolved.");
                    } catch (Exception e) {
                        // pass
                    } finally {
                        pendingReportState.clear(userId);
                    }
                });
            } else {
                // is there a better way to format json in java?
                reporterBotMessagingPort.sendMessageWithBlocks(
                        event.getChannel(),
                        "Hi, what would you like to do?",
                        IncidentActionBlocks.reporterButtons()
                );
            }

            return ctx.ack();
        });
    }
}


