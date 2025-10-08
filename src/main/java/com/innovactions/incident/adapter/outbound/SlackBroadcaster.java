package com.innovactions.incident.adapter.outbound;

import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.domain.service.ChannelNameGenerator;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class SlackBroadcaster implements IncidentBroadcasterPort {

    private final String botToken;
    private final String developerUserId;
    private final ChannelNameGenerator channelNameGenerator;
    private final BotMessagingPort managerBotMessagingPort;
    private final ChannelAdministrationPort channelAdministrationPort;

    @Override
    public void broadcast(Incident incident) {
        // generate channel name based on severity and timestamp
        String channelName = channelNameGenerator.generateChannelName(incident.getSeverity());

        // create new channel in workspace B
        String channelId = channelAdministrationPort.createPublicChannel(channelName);
        if (channelId == null) {
            log.error("Failed to create channel for incident [{}]", incident.getId());
            return;
        }
        log.info("Created channel {} for incident: {}", channelName, incident.getId());

        // set channel topic with reporter information
        channelAdministrationPort.setChannelTopic(channelId, "reporterid:" + incident.getReporterId() + "_slack");

        // invite developer to the channel
        channelAdministrationPort.inviteUsers(channelId, List.of(developerUserId));
        log.info("Invited developer {} to channel {}", developerUserId, channelName);

        // post incident summary with actions to the new channel
        managerBotMessagingPort.sendMessageWithBlocks(
                channelId,
                incident.summary(),
                IncidentActionBlocks.acknowledgeDismissButtons()
        );

        log.info("Incident broadcasted to new channel {}: {}", channelName, incident.getId());
    }
  }
}
