package com.innovactions.incident.adapter.inbound.whatsapp.mapper;

import com.innovactions.incident.adapter.inbound.whatsapp.WhatsAppPayload;
import com.innovactions.incident.application.Platform;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public class WhatsAppPayloadToCreateIncidentCommandMapper {

    public static CreateIncidentCommand toCreateIncidentCommand(WhatsAppPayload payload) {
        var entry = payload.getEntry().get(0);
        var change = entry.getChanges().get(0);
        var value = change.getValue();

        var contact = value.getContacts().get(0);
        String senderName = contact.getProfile().getName();

        var message = value.getMessages().get(0);
        String from = message.getFrom();
        String text = message.getText() != null ? message.getText().getBody() : "";
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(message.getTimestamp()));

        CreateIncidentCommand command = CreateIncidentCommand.builder()
                .reporterId(from)
                .reporterName(senderName)
                .message(text)
                .timestamp(timestamp)
                .platform(Platform.WHATSAPP)
                .build();

        return command;
    }
}

