package com.innovactions.incident.adapter.outbound.Slack;

import com.innovactions.incident.adapter.outbound.IncidentActionBlocks;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.domain.service.ChannelNameGenerator;
import com.innovactions.incident.domain.service.EncryptionService;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class SlackBroadcaster implements IncidentBroadcasterPort {

    private final String developerUserId;
    private final ChannelNameGenerator channelNameGenerator;
    private final BotMessagingPort managerBotMessagingPort;
    private final BotMessagingPort reporterBotMessagingPort;
    private final ChannelAdministrationPort channelAdministrationPort;
    private final EncryptionService encryptionService;

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

        // set channel topic with encrypted reporter information
        String reporterId = incident.getReporterId();
        String encryptedReporterId;
        try {
            encryptedReporterId = encryptionService.encrypt(reporterId);
        } catch (Exception e) {
            log.error("Failed to encrypt reporter ID for incident [{}]: {}", incident.getId(), e.getMessage(), e);
            return null;
        }
        channelAdministrationPort.setChannelTopic(channelId, "reporterid:" + encryptedReporterId + "_" + platform);

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

        reporterBotMessagingPort.sendMessage(
            incident.getReporterId(),
            "✅ Your incident has been reported. Our developers have been notified and will address it shortly."
        );
        
        log.info("Incident broadcasted to new channel {}: {}", channelName, incident.getId());
        return channelId;
    }

    @Override
    public void updateIncidentToDeveloper(Incident incident, String channelId) {
        if (channelId == null) {
            // TODO: prompt the user to ask for the incident ID so we can link to it and update the existing channel
            log.warn("No channelId available for update of incident {}", incident.getId());
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            
            String formattedNow = now.format(formatter);

            managerBotMessagingPort.sendMessage(
                channelId,
                "🔄 Incident update at " + formattedNow + "\n" + incident.getDetails()
            );

            reporterBotMessagingPort.sendMessage(
                incident.getReporterId(),
                "✅ Your incident has been updated. We will keep you informed of any further developments."
            );

            log.info("Updated incident {} posted to channel {}", incident.getId(), channelId);
        } catch (Exception e) {
            log.error("Failed to post update for incident {}", incident.getId(), e);
        }
    }

    @Override
    public void warnUserOfUnlinkedIncident(String reporterId) {
        reporterBotMessagingPort.sendMessage(
            reporterId,
            "⚠️ We couldn't find an active incident to update. Please report a new incident."
        );
    }

    @Override
    public void askUserForMoreInfo(String reporterId) {
        reporterBotMessagingPort.sendMessageWithBlocks(
            reporterId,
            "👋 Hi! A developer is working on the incident you reported. " +
                "Could you please provide more details about the issue to help us resolve it faster?\n\n" +
                "You can reply by clicking the 'Provide Info' button below this chat.\n" +
                "Please reply with any additional information, or steps to reproduce the problem.\n\n" +
                "Thank you for your cooperation!",
            IncidentActionBlocks.askForMoreInfoButtons()
        );
    }
}
