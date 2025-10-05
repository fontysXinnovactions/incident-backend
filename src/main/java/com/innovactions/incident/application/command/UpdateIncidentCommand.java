package com.innovactions.incident.application.command;

import java.time.Instant;

public record UpdateIncidentCommand(
        String channelId,   // Slack channel for this incident
        String message,     // New details from WhatsApp follow-up
        Instant updatedAt   // Timestamp
) {}

