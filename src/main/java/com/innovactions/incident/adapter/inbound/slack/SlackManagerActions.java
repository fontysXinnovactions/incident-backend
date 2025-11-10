package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.adapter.outbound.SlackChannelAdministrationAdapter.ReporterInfo;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.slack.api.bolt.App;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles manager actions in Slack incident channels.
 *
 * <p>Used with the Manager Bot in developer context
 */
@Component
@RequiredArgsConstructor
public class SlackManagerActions {

  private final ChannelAdministrationPort channelAdministrationPort;
  private final BotMessagingPort managerBotMessagingPort;
  private final IncidentBroadcasterPort broadcaster;

  public void register(App managerApp) {
    managerApp.blockAction(
        "ack_incident",
        (req, ctx) -> {
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId();
          managerBotMessagingPort.sendMessage(
              channel, "👨‍💻 <@" + user + "> acknowledged and is working on this incident.");
          return ctx.ack();
        });

    managerApp.blockAction(
        "dismiss_incident",
        (req, ctx) -> {
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId();
          managerBotMessagingPort.sendMessage(channel, "🚫 Incident dismissed by <@" + user + ">.");
          managerBotMessagingPort.sendMessageWithBlocks(
              channel,
              "❓ Do you want to leave the channel?",
              IncidentActionBlocks.leaveChannelButton()
          );
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
          String user = req.getPayload().getUser().getId();
          String channel = req.getPayload().getChannel().getId(); 

          if (user != null) {
            broadcaster.askUserForMoreInfo(user);
            managerBotMessagingPort.sendMessage(channel, "We sent a message to the reporter to as for more details about the incident\n\nWe will update you once we receive a response.");
            return ctx.ack();
          } else {
            managerBotMessagingPort.sendMessage(
                channel,
                "⚠️ Unable to retrieve reporter information. Cannot ask for more details."
            );
            return ctx.ack();
          }
        });
  }
}
