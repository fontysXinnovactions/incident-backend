package com.innovactions.incident.port.outbound;

/** Port interface for sending messages via a bot (Normal DM message, DM with blocks message). */
public interface BotMessagingPort {
  void sendMessage(String channelId, String text);

  void sendMessageWithBlocks(String channelId, String text, String blocksJson);

  String resolveUserRealName(String userId);
}
