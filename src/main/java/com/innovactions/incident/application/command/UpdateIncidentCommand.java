package com.innovactions.incident.application.command;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UpdateIncidentCommand(
        String channelId,   // Slack channel for this incident
        String message,     // New details from WhatsApp follow-up
        Instant updatedAt   // Timestamp
) {}

