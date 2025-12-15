package com.innovactions.incident.adapter.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating Slack action block JSON strings for incident management.
 *
 * <p>Includes methods to create buttons for both the reporter and manager bots.
 */
public final class IncidentActionBlocks {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private IncidentActionBlocks() {
    throw new AssertionError("Utility class should not be instantiated.");
  }

  public static String reporterButtons() {
    return toJson(
        List.of(
            Map.of(
                "type",
                "actions",
                "elements",
                List.of(
                    button("🐞 Report a bug", "report_bug", null),
                    button("🔄️ Update incident", "update_incident", null),
                    button("📊 Check status", "check_status", null)))));
  }

  public static String askForMoreInfoButtons() {
    return toJson(
        List.of(
            Map.of(
                "type",
                "actions",
                "elements",
                List.of(button("Provide Info", "update_incident", "primary")))));
  }

  public static String acknowledgeDismissButtons() {
    return toJson(
        List.of(
            Map.of(
                "type",
                "actions",
                "elements",
                List.of(
                    button("✅ Acknowledge", "ack_incident", "primary"),
                    button("🔄️ Ask for more details", "ask_details", null),
                    button("❌ Dismiss", "dismiss_incident", "danger")))));
  }

  public static String leaveChannelButton() {
    return toJson(
        List.of(
            Map.of(
                "type",
                "actions",
                "elements",
                List.of(button("Leave Channel", "leave_channel", "danger")))));
  }

  public static String askMoreInfoModal(String channelId) {
    return toJson(
        Map.of(
            "type", "modal",
            "callback_id", "ask_more_info_modal",
            "private_metadata", channelId,
            "title", Map.of("type", "plain_text", "text", "Ask for More Info"),
            "submit", Map.of("type", "plain_text", "text", "Send"),
            "close", Map.of("type", "plain_text", "text", "Cancel"),
            "blocks",
                List.of(
                    Map.of(
                        "type",
                        "input",
                        "block_id",
                        "details_block",
                        "label",
                        Map.of("type", "plain_text", "text", "Additional Comments", "emoji", true),
                        "element",
                        Map.of(
                            "type",
                            "plain_text_input",
                            "action_id",
                            "ask_more_info_action",
                            "multiline",
                            true,
                            "placeholder",
                            Map.of("type", "plain_text", "text", "Type your message here..."))))));
  }

  private static Map<String, Object> button(String text, String actionId, String style) {
    var button = new java.util.HashMap<String, Object>();
    button.put("type", "button");
    button.put("text", Map.of("type", "plain_text", "text", text));
    button.put("action_id", actionId);
    if (style != null) {
      button.put("style", style);
    }
    return button;
  }

  private static String toJson(Object obj) {
    return MAPPER.valueToTree(obj).toString();
  }
}
