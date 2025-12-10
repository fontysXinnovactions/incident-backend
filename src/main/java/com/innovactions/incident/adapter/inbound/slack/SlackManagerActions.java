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
            broadcaster.askUserForMoreInfo(reporterInfo.reporterId);
            managerBotMessagingPort.sendMessage(
                channel,
                "We sent a message to the reporter to ask for more details about the incident\n\nWe will update you once we receive a response.");
            return ctx.ack();
          } else {
            managerBotMessagingPort.sendMessage(
                channel,
                "⚠️ Unable to retrieve reporter information. Cannot ask for more details.");
            return ctx.ack();
          }
        });
  }
}
