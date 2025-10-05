package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.ConversationContext;

import java.time.Instant;
import java.util.Optional;

public interface ConversationRepositoryPort {
    Optional<ConversationContext> findActiveByUser(String userId);
    void saveNew(String userId, String incidentId, String channelId, Instant lastMessageAt);
    void update(String userId, String incidentId, String channelId, Instant lastMessageAt);
}
