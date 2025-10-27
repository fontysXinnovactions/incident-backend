package com.innovactions.incident.domain.model;

import java.time.Instant;

/**
 * Represents the state of an active conversation
 * @param userId  WhatsApp phone number; can be extended to support other platforms (e.g. slack and email)
 * @param incidentId UUID from Incident (currently hardcoded for POC)
 * @param channelId Slack channel ID
 * @param lastMessageAt timestamp of the most recent message in the conversation
 * @param active flag indicating whether the conversation is currently active
 */

public record ConversationContext(
        String userId,
        String incidentId,
        String channelId,
        Instant lastMessageAt,
        boolean active
) {}
