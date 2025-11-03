package com.innovactions.incident.application.command;

import com.innovactions.incident.domain.model.Platform;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CreateIncidentCommand(
        // Reporter id specifically refers to a platform
        // specific identification of the user reporting the
        // incident. Example: user id on slack, phone number on whatsapp
        String reporterId,
        String reporterName,
        String message,
        Instant timestamp,
        Platform platform
) {}
