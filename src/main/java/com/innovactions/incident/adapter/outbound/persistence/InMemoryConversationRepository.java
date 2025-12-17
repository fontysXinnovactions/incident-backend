package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.domain.model.ConversationContext;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class InMemoryConversationRepository implements ConversationRepositoryPort {

  private final Map<String, ConversationContext> store = new HashMap<>();

  /**
   * Use case: Retrieves the active conversation context for a given user
   *
   * <p>invoked when a new message is received checks if there is an ongoing conversation for the
   * user
   */
  @Override
  public Optional<ConversationContext> findActiveByUser(String userId) {
    return Optional.ofNullable(store.get(userId));
  }

  /**
   * Use case: Saves new conversation context for a given user
   *
   * <p>invoked when an incident is first created or when an existing conversation has expired.
   */
  @Override
  public void saveNew(String userId, String incidentId, String channelId, Instant lastMessageAt) {
    var ctx = new ConversationContext(userId, incidentId, channelId, lastMessageAt, true);
    store.put(userId, ctx);
    log.info("Saved new context for {} -> {}", userId, ctx);
    log.debug("Current store: {}", store);
  }

  /**
   * Use case: Refreshes an existing conversation context for the user.
   *
   * <p>Called when a message arrives and conversation context for the user already existed and not
   * expired
   */
  @Override
  public void update(String userId, String incidentId, String channelId, Instant lastMessageAt) {
    ConversationContext ctx =
        new ConversationContext(userId, incidentId, channelId, lastMessageAt, true);
    store.put(userId, ctx);
    log.info("Updated context for user {} -> {}", userId, ctx);
    log.debug("Current store: {}", store);
  }
}
