package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles the flow of the Reporter Bot and show actions that can be taken in Slack.
 *
 * <p>Used with the Reporter Bot in client context
 */
@Component
@RequiredArgsConstructor
public class SlackReporterFlow {

  private final PendingReportState pendingReportState;
  private final IncidentInboundPort incidentInboundPort;
  private final BotMessagingPort reporterBotMessagingPort;

  public void register(App app) {
    // report_bug --> mark pending and prompt for details
    app.blockAction(
        "report_bug",
        (req, ctx) -> {
          String userId = req.getPayload().getUser().getId();
          pendingReportState.markPending(userId);
          reporterBotMessagingPort.sendMessage(
              userId,
              "<@"
                  + userId
                  + ">, please describe the bug in detail.\n"
                  + "A great way to describe your incident is to be clear and specific. "
                  + "Here's a sample for you to follow:\n\n"
                  + "*Steps to Reproduce:*\n"
                  + "1. Open the app\n"
                  + "2. Click on 'Submit'\n"
                  + "3. Observe the error\n\n"
                  + "*Expected Result:*\n"
                  + "The app should submit the form without errors.\n\n"
                  + "*Actual Result:*\n"
                  + "An error message is displayed and the form is not submitted."
                  + "\n\nNow it's your turn! Provide similar details for your incident.");
          return ctx.ack();
        });

    // check_status --> check status of current incident that is assigned to the user
    app.blockAction(
        "check_status",
        (req, ctx) -> {
          String userId = req.getPayload().getUser().getId();
          // for now:
          // check status of pending report state and update report state
          String isUserPending = pendingReportState.isPending(userId) ? "pending" : "not pending";
          String isUserUpdating =
              pendingReportState.isUpdating(userId) ? "updating" : "not updating";
          reporterBotMessagingPort.sendMessage(
              userId,
              "<@"
                  + userId
                  + ">, your report is currently "
                  + isUserPending
                  + " and is "
                  + isUserUpdating
                  + ".");
          return ctx.ack();
        });

    // update_incident --> prompt user to provide updated details
    app.blockAction(
        "update_incident",
        (req, ctx) -> {
          String userId = req.getPayload().getUser().getId();
          pendingReportState.markUpdating(userId);
          reporterBotMessagingPort.sendMessage(
              userId, "<@" + userId + ">, please provide some updated details of your incident.");
          return ctx.ack();
        });

    // if pending, treat message as incident details, otherwise show actions
    app.event(
        MessageEvent.class,
        (payload, ctx) -> {
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
            CompletableFuture.runAsync(
                () -> {
                  try {
                    String reporterName = reporterBotMessagingPort.resolveUserRealName(userId);
                    CreateIncidentCommand command =
                        new CreateIncidentCommand(
                            userId, reporterName, text, Instant.now(), Platform.SLACK);
                    incidentInboundPort.reportIncident(command);
                  } catch (Exception e) {
                    // pass
                  } finally {
                    pendingReportState.clearPending(userId);
                  }
                });
          } else if (pendingReportState.isUpdating(userId)) {
            reporterBotMessagingPort.sendMessage(userId, "Thanks! Processing your update...");
            CompletableFuture.runAsync(
                () -> {
                  try {
                    CreateIncidentCommand updateCommand =
                        new CreateIncidentCommand(
                            userId,
                            reporterBotMessagingPort.resolveUserRealName(userId),
                            text,
                            Instant.now(),
                            Platform.SLACK);
                    incidentInboundPort.updateExistingIncident(updateCommand);
                  } catch (Exception e) {
                    // pass
                  } finally {
                    pendingReportState.clearUpdating(userId);
                  }
                });
          } else {
            // is there a better way to format json in java?
            reporterBotMessagingPort.sendMessageWithBlocks(
                event.getChannel(),
                "Hi, you are now talking to a bot. Please use the button to choose what you would like to do.",
                IncidentActionBlocks.reporterButtons());
          }

          return ctx.ack();
        });
  }
}
