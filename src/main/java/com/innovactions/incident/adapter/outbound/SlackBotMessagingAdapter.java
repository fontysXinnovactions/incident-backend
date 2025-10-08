package com.innovactions.incident.adapter.outbound;

import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SlackBotMessagingAdapter implements BotMessagingPort {

    private final String botToken;

    public SlackBotMessagingAdapter(String botToken) {
        this.botToken = botToken;
    }

    @Override
    public void sendMessage(String channelId, String text) {
        sendMessageInternal(channelId, text, null);
    }

    @Override
    public void sendMessageWithBlocks(String channelId, String text, String blocksJson) {
        sendMessageInternal(channelId, text, blocksJson);
    }

    private void sendMessageInternal(String channelId, String text, String blocksJson) {
        try {
            ChatPostMessageResponse response;
            if (blocksJson != null) {
                response = Slack.getInstance().methods(botToken)
                        .chatPostMessage(req -> req
                                .channel(channelId)
                                .text(text)
                                .blocksAsString(blocksJson)
                        );
            } else {
                response = Slack.getInstance().methods(botToken)
                        .chatPostMessage(req -> req
                                .channel(channelId)
                                .text(text)
                        );
            }
            
            if (!response.isOk()) {
                log.warn("Failed to send message to {}: {}", channelId, response.getError());
            } else {
                log.info("Message sent to {}: {}", channelId, text);
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error sending message to {}: {}", channelId, e.getMessage(), e);
        }
    }

    @Override
    public String resolveUserRealName(String userId) {
        try {
            UsersInfoResponse info = Slack.getInstance().methods(botToken).usersInfo(r -> r.user(userId));
            if (info.isOk() && info.getUser() != null && info.getUser().getProfile() != null) {
                return info.getUser().getProfile().getRealName();
            }
        } catch (IOException | SlackApiException e) {
            log.warn("Slack API error resolving user info for {}: {}", userId, e.getMessage());
        }
        return userId;
    }
}


