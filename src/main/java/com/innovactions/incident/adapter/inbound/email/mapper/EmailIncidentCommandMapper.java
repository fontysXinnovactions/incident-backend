package com.innovactions.incident.adapter.inbound.email.mapper;

import com.innovactions.incident.adapter.inbound.email.model.EmailMessage;
import com.innovactions.incident.application.Platform;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public final class EmailIncidentCommandMapper {

    /**
     * Maps {@link EmailMessage} to {@link CreateIncidentCommand}.
     */
    public static CreateIncidentCommand map(EmailMessage email) {

        // Reporter info (sender)
        String reporterId = email.getFrom().getEmailAddress().getAddress();
        String reporterName = email.getFrom().getEmailAddress().getName();

        // Message details
        String subject = email.getSubject() != null ? email.getSubject() : "";
        String body = email.getBodyPreview() != null ? email.getBodyPreview() : "";
        Instant timestamp = Instant.parse(email.getReceivedDateTime());


        // Build command
        return CreateIncidentCommand.builder()
                .reporterId(reporterId)
                .reporterName(reporterName)
                .message(subject + "\n\n" + body)
                .timestamp(timestamp)
                .platform(Platform.EMAIL)
                .build();
    }
}
