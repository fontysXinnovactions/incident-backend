package com.innovactions.incident.port.outbound;

public interface BotMessagingPort {
  void sendMessage(String channelId, String text);

  void sendMessageWithBlocks(String channelId, String text, String blocksJson);

  String resolveUserRealName(String userId);
}
