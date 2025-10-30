package com.innovactions.incident.port.outbound;

/**
 * <p>Port interface for sending messages via a bot (Normal DM message, DM with blocks message).</p>
 */
public interface BotMessagingPort {
    void sendMessage(String channelId, String text);
    void sendMessageWithBlocks(String channelId, String text, String blocksJson);
    String resolveUserRealName(String userId);
}


