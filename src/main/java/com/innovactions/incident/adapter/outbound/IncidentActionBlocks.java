package com.innovactions.incident.adapter.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>Utility class for generating Slack action block JSON strings for incident management.</p>
 * <p>Includes methods to create buttons for both the reporter and manager bots.</p>
 */
public final class IncidentActionBlocks {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String reporterButtons() {
        return toJson(List.of(
                Map.of(
                    "type", "actions",
                    "elements", List.of(
                        button("🐞 Report a bug", "report_bug", null),
                        button("📊 Check status", "check_status", null)
                    )
                )
        ));
    }

    public static String acknowledgeDismissButtons() {
        return toJson(List.of(
                Map.of(
                    "type", "actions",
                    "elements", List.of(
                        button("✅ Acknowledge", "ack_incident", "primary"),
                        button("❌ Dismiss", "dismiss_incident", "danger")
                    )
                )
        ));
    }

    public static String leaveChannelButton() {
        return toJson(List.of(
                Map.of(
                    "type", "actions",
                    "elements", List.of(
                        button("Leave Channel", "leave_channel", "danger")
                    )
                )
        ));
    }

    private static Map<String, Object> button(String text, String actionId, String style) {
        var button = Map.<String, Object>of(
                "type", "button",
                "text", Map.of("type", "plain_text", "text", text),
                "action_id", actionId
        );

        if (style == null) return button;

        return Map.of(
                "type", "button",
                "text", Map.of("type", "plain_text", "text", text),
                "style", style,
                "action_id", actionId
        );
    }

    private static String toJson(Object obj) {
        return MAPPER.valueToTree(obj).toString();
    }
}


