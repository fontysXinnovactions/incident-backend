package com.innovactions.incident.port.outbound;

import java.util.List;

import com.innovactions.incident.adapter.outbound.SlackChannelAdministrationAdapter.ReporterInfo;

/**
 * Port interface for Slack channel administration tasks (Create channel, set topic, invite/kick
 * users).
 */
public interface ChannelAdministrationPort {
  String createPublicChannel(String name);

  void setChannelTopic(String channelId, String topic);

  ReporterInfo extractReporterIdFromTopic(String channelId);

  void inviteUsers(String channelId, List<String> userIds);

  void kickUserFromChannel(String channelId, String userId);

  List<String> listMembers(String channelId);
}
