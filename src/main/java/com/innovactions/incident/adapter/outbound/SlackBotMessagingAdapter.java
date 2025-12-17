package com.innovactions.incident.adapter.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for sending messages via a Slack bot (Normal DM message, DM with blocks message).
 *
 * <p>Implements the BotMessagingPort interface for Slack platform.
 */
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
        // If caller supplied both text and blocks, ensure the text is visible by prepending
        // a "section" block. Slack ignores the top-level text when blocks are present.
        final String effectiveBlocks =
            (text != null && !text.isBlank()) ? prependTextSection(blocksJson, text) : blocksJson;
        response =
            Slack.getInstance()
                .methods(botToken)
                .chatPostMessage(
                    req -> req.channel(channelId).text(text).blocksAsString(effectiveBlocks));
      } else {
        response =
            Slack.getInstance()
                .methods(botToken)
                .chatPostMessage(req -> req.channel(channelId).text(text));
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

  private String prependTextSection(String blocksJson, String text) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(blocksJson.getBytes(StandardCharsets.UTF_8));
      ArrayNode blocksArray;
      if (node == null || !node.isArray()) {
        blocksArray = mapper.createArrayNode();
      } else {
        blocksArray = (ArrayNode) node;
      }

      ObjectNode section = mapper.createObjectNode();
      section.put("type", "section");
      ObjectNode mrkdwn = mapper.createObjectNode();
      mrkdwn.put("type", "mrkdwn");
      mrkdwn.put("text", text);
      section.set("text", mrkdwn);

      ArrayNode newArray = mapper.createArrayNode();
      newArray.add(section);
      newArray.addAll(blocksArray);
      return mapper.writeValueAsString(newArray);
    } catch (Exception e) {
      log.warn("Failed to prepend section to blocks: {}", e.getMessage());
      return blocksJson;
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
