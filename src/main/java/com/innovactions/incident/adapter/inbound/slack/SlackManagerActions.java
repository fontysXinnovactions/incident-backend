package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.slack.api.bolt.App;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackManagerActions {

    private final String botTokenB;
    private final ChannelAdministrationPort channelAdministrationPort;
    private final BotMessagingPort managerBotMessagingPort;

    public void register(App managerApp) {
        managerApp.blockAction("ack_incident", (req, ctx) -> {
            String user = req.getPayload().getUser().getId();
            String channel = req.getPayload().getChannel().getId();
            managerBotMessagingPort.sendMessage(channel, "👨‍💻 <@" + user + "> acknowledged and is working on this incident.");
            return ctx.ack();
        });

        managerApp.blockAction("dismiss_incident", (req, ctx) -> {
            String user = req.getPayload().getUser().getId();
            String channel = req.getPayload().getChannel().getId();
            managerBotMessagingPort.sendMessage(channel, "🚫 Incident dismissed by <@" + user + ">.");
            // Post a follow-up with a "Leave Channel" button
            managerBotMessagingPort.sendMessageWithBlocks(
                    channel,
                    "❓ Do you want to leave the channel?",
                    IncidentActionBlocks.leaveChannelButton()
            );
            return ctx.ack();
        });

        managerApp.blockAction("leave_channel", (req, ctx) -> {
            String user = req.getPayload().getUser().getId();
            String channel = req.getPayload().getChannel().getId();
            channelAdministrationPort.kickUserFromChannel(channel, user);
            return ctx.ack();
        });
    }
}


