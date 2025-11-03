package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.ConversationContext;
import java.time.Instant;
import java.util.Optional;

/**
 * Use case: Store conversation gets existing conversation context of users saves a conversation
 * after the incident is created Updates an ongoing conversation
 */
public interface ConversationRepositoryPort {
  Optional<ConversationContext> findActiveByUser(String userId);

  void saveNew(String userId, String incidentId, String channelId, Instant lastMessageAt);

  void update(String userId, String incidentId, String channelId, Instant lastMessageAt);
}
