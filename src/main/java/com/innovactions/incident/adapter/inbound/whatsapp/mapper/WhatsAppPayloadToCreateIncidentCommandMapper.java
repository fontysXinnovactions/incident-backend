package com.innovactions.incident.adapter.inbound.whatsapp.mapper;

import com.innovactions.incident.adapter.inbound.whatsapp.WhatsAppPayload;
import com.innovactions.incident.application.Platform;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public final class WhatsAppPayloadToCreateIncidentCommandMapper {

    /**
     * Maps {@link WhatsAppPayload} to {@link CreateIncidentCommand}.
     *
     * @param payload the incoming WhatsApp payload
     * @return a {@link CreateIncidentCommand} instance
     * @throws IllegalArgumentException if the payload is missing required fields
     */
    public static CreateIncidentCommand map(WhatsAppPayload payload) {
        var entry = payload.getEntry().getFirst();
        var change = entry.getChanges().getFirst();
        var value = change.getValue();
        var contact = value.getContacts().getFirst();

        String senderName = contact.getProfile().getName();
        var message = value.getMessages().getFirst();
        String from = message.getFrom();
        String text = message.getText() != null ? message.getText().getBody() : "";
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(message.getTimestamp()));

        return CreateIncidentCommand.builder()
                .reporterId(from)
                .reporterName(senderName)
                .message(text)
                .timestamp(timestamp)
                .platform(Platform.WHATSAPP)
                .build();
    }
}
