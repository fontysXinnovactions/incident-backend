package com.innovactions.incident.adapter.outbound.Slack;

import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.IncidentClosurePort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsInfoResponse;
import com.slack.api.methods.response.conversations.ConversationsKickResponse;
import com.slack.api.methods.response.conversations.ConversationsMembersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SlackIncidentClosureBroadcaster implements IncidentClosurePort {
    private final String botTokenB;
    private final BotMessagingPort reporterBotMessagingPort;
    private final BotMessagingPort managerBotMessagingPort;

    @Override
    public String getPlatformName() {
        return "Slack";
    }

    public void closeIncident(String developerUserId, String channelId, String reason) {
        try {
            // announce closure in the incident channel
            announceClosure(channelId, developerUserId, reason);

            // extract reporter information from channel topic
            ReporterInfo reporterInfo = extractReporterFromTopic(channelId);

            // remove all members from the incident channel
            removeAllMembers(channelId);

            // notify original reporter in workspace A
            if (reporterInfo != null) {
                notifyReporter(reporterInfo.reporterId, reason);
            }

        } catch (Exception e) {
            log.error("Error closing incident in channel {}: {}", channelId, e.getMessage(), e);
        }
    }

    private void announceClosure(String channelId, String developerUserId, String reason) {
        managerBotMessagingPort.sendMessage(channelId, "✅ Incident closed by <@" + developerUserId + ">. Reason: " + reason);
    }

    private ReporterInfo extractReporterFromTopic(String channelId) {
        try {
            ConversationsInfoResponse response =
                    Slack.getInstance().methods(botTokenB).conversationsInfo(req -> req.channel(channelId));

            if (!response.isOk()) {
                log.error("Failed to get channel info for {}: {}", channelId, response.getError());
                return null;
            }

            String topic = response.getChannel().getTopic().getValue();
            if (topic != null && topic.contains("reporterid:")) {
                String[] parts = topic.split("reporterid:")[1].trim().split("_");
                if (parts.length >= 2) {
                    String reporterId = parts[0];
                    String platform = parts[1];
                    return new ReporterInfo(reporterId, platform);
                }
            }
            return null;
        } catch (IOException | SlackApiException e) {
            log.error(
                    "Error extracting reporter from topic for channel {}: {}", channelId, e.getMessage(), e);
            return null;
        }
    }

    private void removeAllMembers(String channelId) {
        try {
            ConversationsMembersResponse membersResponse = Slack.getInstance().methods(botTokenB)
                    .conversationsMembers(req -> req.channel(channelId));

            managerBotMessagingPort.sendMessage(channelId, "Removing all members from channel " + channelId);

            if (!membersResponse.isOk()) {
                log.error("Failed to get members for channel {}: {}", channelId, membersResponse.getError());
                return;
            }

            List<String> members = membersResponse.getMembers();
            for (String member : members) {
                try {
                    ConversationsKickResponse kickResponse = Slack.getInstance().methods(botTokenB)
                            .conversationsKick(req -> req
                                    .channel(channelId)
                                    .user(member)
                            );

                    if (!kickResponse.isOk()) {
                        log.warn("Failed to remove member {} from channel {}: {}", member, channelId, kickResponse.getError());
                    } else {
                        log.info("Removed member {} from channel {}", member, channelId);
                    }
                } catch (IOException | SlackApiException e) {
                    log.warn("Error removing member {} from channel {}: {}", member, channelId, e.getMessage());
                }
            }

        } catch (IOException | SlackApiException e) {
            log.error("Error extracting reporter from topic for channel {}: {}", channelId, e.getMessage(), e);
        }
    }

    private void notifyReporter(String reporterId, String reason) {
        reporterBotMessagingPort.sendMessage(reporterId, "✅ Your reported incident has been closed.\nReason: " + reason);
    }

    public void kickUserFromChannel(String channelId, String userId) {
        try {
            Slack.getInstance().methods(botTokenB)
                    .conversationsKick(req -> req
                            .channel(channelId)
                            .user(userId)
                    );
        } catch (IOException | SlackApiException e) {
            log.error("Error kicking user from channel {}: {}", channelId, e.getMessage(), e);
        }
        managerBotMessagingPort.sendMessage(channelId, "👋 <@" + userId + "> has left the channel.");
    }

    private static class ReporterInfo {
        final String reporterId;
        final String platform;

        ReporterInfo(String reporterId, String platform) {
            this.reporterId = reporterId;
            this.platform = platform;
        }
    }
}