package com.innovactions.incident.application.command;

import java.time.Instant;
import lombok.Builder;

@Builder
public record UpdateIncidentCommand(
    String channelId, // Slack channel for this incident
    String message, // New details from follow-up
    Instant updatedAt // Timestamp
    ) {}
