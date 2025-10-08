package com.innovactions.incident.adapter.outbound;

public final class IncidentActionBlocks {

    private IncidentActionBlocks() {}

    public static String reporterButtons() {
        return "[" +
                "{\n" +
                "  \"type\": \"actions\",\n" +
                "  \"elements\": [\n" +
                "    {\n" +
                "      \"type\": \"button\",\n" +
                "      \"text\": { \"type\": \"plain_text\", \"text\": \"🐞 Report a bug\" },\n" +
                "      \"action_id\": \"report_bug\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"button\",\n" +
                "      \"text\": { \"type\": \"plain_text\", \"text\": \"📊 Check status\" },\n" +
                "      \"action_id\": \"check_status\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "]";
    }

    public static String acknowledgeDismissButtons() {
        return "[" +
                "{\n" +
                "  \"type\": \"actions\",\n" +
                "  \"elements\": [\n" +
                "    {\n" +
                "      \"type\": \"button\",\n" +
                "      \"text\": { \"type\": \"plain_text\", \"text\": \"✅ Acknowledge\" },\n" +
                "      \"style\": \"primary\",\n" +
                "      \"action_id\": \"ack_incident\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"button\",\n" +
                "      \"text\": { \"type\": \"plain_text\", \"text\": \"❌ Dismiss\" },\n" +
                "      \"style\": \"danger\",\n" +
                "      \"action_id\": \"dismiss_incident\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "]";
    }

    public static String leaveChannelButton() {
        return "[" +
                "{\n" +
                "  \"type\": \"actions\",\n" +
                "  \"elements\": [\n" +
                "    {\n" +
                "      \"type\": \"button\",\n" +
                "      \"text\": { \"type\": \"plain_text\", \"text\": \"Leave Channel\" },\n" +
                "      \"style\": \"danger\",\n" +
                "      \"action_id\": \"leave_channel\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "]";
    }
}


