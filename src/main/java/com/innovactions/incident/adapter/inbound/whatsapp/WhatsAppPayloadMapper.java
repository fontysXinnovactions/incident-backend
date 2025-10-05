package com.innovactions.incident.adapter.inbound.whatsapp;

import com.innovactions.incident.application.InboundMessage;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import java.time.Instant;

public class WhatsAppPayloadMapper {
    private WhatsAppPayloadMapper() {
        // utility class
    }

    public static InboundMessage toInboundMessage(WhatsAppPayload payload) {
        var entry = payload.getEntry().get(0);
        var change = entry.getChanges().get(0);
        var value = change.getValue();

        var contact = value.getContacts().get(0);
        String senderName = contact.getProfile().getName();

        var message = value.getMessages().get(0);
        String from = message.getFrom();
        String text = message.getText() != null ? message.getText().getBody() : "";
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(message.getTimestamp()));

        return new InboundMessage("whatsapp", from, senderName, text, timestamp);
    }
}
//    public static CreateIncidentCommand toIncidentCommand(WhatsAppPayload payload) {
//        var entry = payload.getEntry().get(0);
//        var change = entry.getChanges().get(0);
//        var value = change.getValue();
//
//        var contact = value.getContacts().get(0);
//        String senderName = contact.getProfile().getName();
//
//        var message = value.getMessages().get(0);
//        String text = message.getText() != null ? message.getText().getBody() : "";
//        String reporterId = message.getFrom();
//        Instant reportedAt = Instant.ofEpochSecond(Long.parseLong(message.getTimestamp()));
//
//              return new CreateIncidentCommand(
////                      reporterId,   // real WhatsApp user ID
//                "WhatsApp",   // reporterId
//                senderName,   // reporterName
//                text,         // message
////              Instant.now() // reportedAt
//                reportedAt
//        );
//
//    }
//}

