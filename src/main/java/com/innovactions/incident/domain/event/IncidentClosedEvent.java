package com.innovactions.incident.domain.event;

public record IncidentClosedEvent(
        String reporterId,
        String platform,
        String reason
) {}