package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import com.innovactions.incident.port.outbound.ReporterInfo;
import com.slack.api.bolt.App;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles manager actions in Slack incident channels.
 *
 * <p>Used with the Manager Bot in developer context
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlackManagerActions {

  private final ChannelAdministrationPort channelAdministrationPort;
  private final BotMessagingPort managerBotMessagingPort;
  private final IncidentBroadcasterPort broadcaster;
  private final IncidentPersistencePort incidentPersistencePort;

  public void register(App managerApp) {
    managerApp.blockAction(
        "ack_incident",
        (req, ctx) -> {
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId();

          // Mark incident as ASSIGNED and set assignee to this developer, if linked
          incidentPersistencePort
              .findBySlackChannelId(channel)
              .ifPresent(
                  entity -> {
                    try {
                      incidentPersistencePort.assignToDeveloper(entity.getId(), user);
                    } catch (Exception e) {
                      // log but don't break the UX
                      log.error("Failed to assign incident to developer: {}", e.getMessage());
                    }
                  });

          managerBotMessagingPort.sendMessage(
              channel, "👨‍💻 <@" + user + "> acknowledged and is working on this incident.");
          return ctx.ack();
        });

    managerApp.blockAction(
        "dismiss_incident",
        (req, ctx) -> {
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId();

          // Mark incident as DISMISSED in the database, if linked
          incidentPersistencePort
              .findBySlackChannelId(channel)
              .ifPresent(
                  entity -> {
                    try {
                      incidentPersistencePort.updateIncidentStatus(
                          entity.getId().toString(), Status.DISMISSED);
                    } catch (Exception e) {
                      // log but don't break the UX
                      log.error("Failed to update status to dismiss: {}", e.getMessage());
                    }
                  });

          managerBotMessagingPort.sendMessage(channel, "🚫 Incident dismissed by <@" + user + ">.");
          managerBotMessagingPort.sendMessageWithBlocks(
              channel,
              "❓ Do you want to leave the channel?",
              IncidentActionBlocks.leaveChannelButton());
          return ctx.ack();
        });

    managerApp.blockAction(
        "leave_channel",
        (req, ctx) -> {
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId();
          channelAdministrationPort.kickUserFromChannel(channel, user);
          return ctx.ack();
        });

    managerApp.blockAction(
        "ask_details",
        (req, ctx) -> {
          String channel = req.getPayload().getChannel().getId();

          ReporterInfo reporterInfo = channelAdministrationPort.extractReporterIdFromTopic(channel);

          if (reporterInfo != null) {
            // open a modal with input and submit button
            var modalJson = IncidentActionBlocks.askMoreInfoModal(channel);
            ctx.client()
                .viewsOpen(
                    r -> r.triggerId(req.getPayload().getTriggerId()).viewAsString(modalJson));
            return ctx.ack();
          } else {
            managerBotMessagingPort.sendMessage(
                channel,
                "⚠️ Unable to retrieve reporter information. Cannot ask for more details.");
            return ctx.ack();
          }
        });

    managerApp.viewSubmission(
        "ask_more_info_modal",
        (req, ctx) -> {
          var view = req.getPayload().getView();
          String channelId = view.getPrivateMetadata();

          var stateValues = view.getState().getValues();
          String developerMessage = stateValues.get("details_block").get("ask_more_info_action").getValue();

          ReporterInfo reporterInfo =
              channelAdministrationPort.extractReporterIdFromTopic(channelId);
          if (reporterInfo != null) {
            broadcaster.askUserForMoreInfo(reporterInfo.reporterId, developerMessage);

            managerBotMessagingPort.sendMessage(
                channelId,
                "✅ Additional details sent to the reporter. We will update you once they respond.");
          } else {
            managerBotMessagingPort.sendMessage(
                channelId,
                "⚠️ Unable to retrieve reporter information. Cannot send additional details.");
          }

          return ctx.ack();
        });
  }
}
