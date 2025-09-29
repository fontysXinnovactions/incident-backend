package com.innovactions.incident.adapter.inbound;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class WhatsAppPayloadMapper {
    private WhatsAppPayloadMapper() {
        // utility class
    }
    public static CreateIncidentCommand toIncidentCommand(Map<String, Object> body) {
        // Navigate payload: entry[0] → changes[0] → value
        Map entry = ((List<Map>) body.get("entry")).get(0);
        Map change = ((List<Map>) entry.get("changes")).get(0);
        Map value = (Map) change.get("value");

        // Contact info
        Map contact = ((List<Map>) value.get("contacts")).get(0);
        String senderName = (String) ((Map) contact.get("profile")).get("name");

        // Message
        Map message = ((List<Map>) value.get("messages")).get(0);
        String text = (String) ((Map) message.get("text")).get("body");

              return new CreateIncidentCommand(
                "WhatsApp",   // reporterId
                senderName,   // reporterName
                text,         // message
                Instant.now() // reportedAt
        );

    }
}
// Build Incident
//        return new Incident(
//                "WhatsApp",     // reporterId (fixed for POC)
//                senderName,     // reporterName
//                text,
//                Severity.MINOR, // TODO: integrate classifier
//                "Unassigned"
//        );
