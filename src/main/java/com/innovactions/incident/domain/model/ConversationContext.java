package com.innovactions.incident.domain.model;

import java.time.Instant;

public record ConversationContext(
        String userId,      // WhatsApp wa_id (phone number)
        String incidentId,  // UUID from Incident (business ID, optional for POC)
        String channelId,   // Slack channelId
        Instant lastMessageAt,
        boolean active
) {}
