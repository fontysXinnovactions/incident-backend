package com.innovactions.incident.domain.event;

/**
 * Domain event published when an incident has been closed.
 *
 * <p>Carries information about the reporter, the platform where the incident was created, and the
 * reason for closure.
 */
public record IncidentClosedEvent(
        String slackChannel,
        String reporterId,
        String platform,
        String reason
) {}
