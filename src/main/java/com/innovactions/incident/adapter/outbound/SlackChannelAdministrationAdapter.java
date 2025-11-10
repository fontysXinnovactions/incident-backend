package com.innovactions.incident.adapter.outbound;

import com.innovactions.incident.domain.service.EncryptionService;
import com.innovactions.incident.port.outbound.ChannelAdministrationPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.*;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for Slack channel administration tasks (Create channel, set topic, invite/kick users).
 *
 * <p>Implements the ChannelAdministrationPort interface for Slack platform.
 */
@Slf4j
public class SlackChannelAdministrationAdapter implements ChannelAdministrationPort {

  private final String botToken;
  private final EncryptionService encryptionService;

  public SlackChannelAdministrationAdapter(String botToken, EncryptionService encryptionService) {
    this.botToken = botToken;
    this.encryptionService = encryptionService;
  }

  @Override
  public String createPublicChannel(String name) {
    try {
      ConversationsCreateResponse res =
          Slack.getInstance()
              .methods(botToken)
              .conversationsCreate(req -> req.name(name).isPrivate(false));
      if (!res.isOk()) {
        log.error("Failed to create channel {}: {}", name, res.getError());
        return null;
      }
      log.info("Created channel {} for incident: {}", name, res.getChannel().getId());
      return res.getChannel().getId();
    } catch (IOException | SlackApiException e) {
      log.error("Error creating channel {}: {}", name, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void setChannelTopic(String channelId, String topic) {
    try {
      ConversationsSetTopicResponse res =
          Slack.getInstance()
              .methods(botToken)
              .conversationsSetTopic(req -> req.channel(channelId).topic(topic));
      if (!res.isOk()) {
        log.warn("Failed to set topic for {}: {}", channelId, res.getError());
      }
    } catch (IOException | SlackApiException e) {
      log.error("Error setting topic for {}: {}", channelId, e.getMessage(), e);
    }
  }
  
  @Override
  public ReporterInfo extractReporterIdFromTopic(String channelId) {
    try {
      ConversationsInfoResponse response =
          Slack.getInstance().methods(botToken).conversationsInfo(req -> req.channel(channelId));

      if (!response.isOk()) {
        log.error("Failed to get channel info for {}: {}", channelId, response.getError());
        return null;
      }

      String topic = response.getChannel().getTopic().getValue();
      if (topic != null && topic.contains("reporterid:")) {
        String[] parts = topic.split("reporterid:")[1].trim().split("_");
        if (parts.length >= 2) {
          String reporterId = parts[0];
          try {
            reporterId = encryptionService.decrypt(reporterId);
          } catch (Exception e) {
            log.error(
                "Failed to decrypt reporter ID for channel {}: {}", channelId, e.getMessage(), e);
            return null;
          }
          String platform = parts[1];
          return new ReporterInfo(reporterId, platform);
        }
      }
    } catch (IOException | SlackApiException e) {
      log.error("Error extracting reporter ID from topic for {}: {}", channelId, e.getMessage(), e);
      return null;
    }
    return null;
  }

  @Override
  public void inviteUsers(String channelId, List<String> userIds) {
    try {
      ConversationsInviteResponse res =
          Slack.getInstance()
              .methods(botToken)
              .conversationsInvite(req -> req.channel(channelId).users(userIds));
      if (!res.isOk()) {
        log.warn("Failed to invite users to {}: {}", channelId, res.getError());
      }
      log.info("Invited users to channel {}: {}", channelId, String.join(", ", userIds));
    } catch (IOException | SlackApiException e) {
      log.error("Error inviting users to {}: {}", channelId, e.getMessage(), e);
    }
  }

  @Override
  public void kickUserFromChannel(String channelId, String userId) {
    try {
      ConversationsKickResponse res =
          Slack.getInstance()
              .methods(botToken)
              .conversationsKick(req -> req.channel(channelId).user(userId));
      if (!res.isOk()) {
        log.warn("Failed to kick {} from {}: {}", userId, channelId, res.getError());
      }
    } catch (IOException | SlackApiException e) {
      log.error("Error kicking {} from {}: {}", userId, channelId, e.getMessage(), e);
    }
  }

  @Override
  public List<String> listMembers(String channelId) {
    try {
      ConversationsMembersResponse res =
          Slack.getInstance().methods(botToken).conversationsMembers(req -> req.channel(channelId));
      if (!res.isOk()) {
        log.warn("Failed to list members for {}: {}", channelId, res.getError());
        return List.of();
      }
      return res.getMembers();
    } catch (IOException | SlackApiException e) {
      log.error("Error listing members for {}: {}", channelId, e.getMessage(), e);
      return List.of();
    }
  }

  public static class ReporterInfo {
    public final String reporterId;
    public final String platform;

    public ReporterInfo(String reporterId, String platform) {
      this.reporterId = reporterId;
      this.platform = platform;
    }
  }
}
