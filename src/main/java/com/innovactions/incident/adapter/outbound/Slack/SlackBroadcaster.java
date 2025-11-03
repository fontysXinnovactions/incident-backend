package com.innovactions.incident.adapter.outbound.Slack;

import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.service.ChannelNameGenerator;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SlackBroadcaster implements IncidentBroadcasterPort {

    private final String developerUserId;
    private final ChannelNameGenerator channelNameGenerator;
    private final BotMessagingPort managerBotMessagingPort;
    private final ChannelAdministrationPort channelAdministrationPort;

    @Override
    public String initSlackDeveloperWorkspace(Incident incident, Platform platform) {
        // generate channel name based on severity and timestamp
        String channelName = channelNameGenerator.generateChannelName(incident.getSeverity());

        // create new channel in workspace B
        String channelId = channelAdministrationPort.createPublicChannel(channelName);
        if (channelId == null) {
            log.error("Failed to create channel for incident [{}]", incident.getId());
            return null;
        }

        // set channel topic with reporter information
        channelAdministrationPort.setChannelTopic(channelId, "reporterid:" + incident.getReporterId() + "_" + platform);

        // invite developer to the channel
        channelAdministrationPort.inviteUsers(channelId, List.of(developerUserId));

        // send incident summary to the new channel
        managerBotMessagingPort.sendMessage(channelId, incident.summary());

        // post incident summary with actions to the new channel
        managerBotMessagingPort.sendMessageWithBlocks(
                channelId,
                incident.summary(),
                IncidentActionBlocks.acknowledgeDismissButtons()
        );

        log.info("Incident broadcasted to new channel {}: {}", channelName, incident.getId());
        return channelId;
    }

    @Override
    public void updateIncidentToDeveloper(Incident incident, String channelId) {
        // if the channel exists and is active, update by sending the new message input
        if (channelId == null) {
            log.warn("No channelId available for update of incident {}", incident.getId());
            return;
        }
        try {
            managerBotMessagingPort.sendMessage(
                    channelId,
                    "🔄 Incident update at " + Instant.now() + incident.getDetails()
            );
            log.info("Updated incident {} posted to channel {}", incident.getId(), channelId);
        } catch (Exception e) {
            log.error("Failed to post update for incident {}", incident.getId(), e);
        }
    }
}