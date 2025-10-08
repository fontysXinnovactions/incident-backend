package com.innovactions.incident.persistence;

import com.innovactions.incident.domain.model.ConversationContext;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryConversationRepository implements ConversationRepositoryPort {

    private final Map<String, ConversationContext> store = new HashMap<>();

    @Override
    public Optional<ConversationContext> findActiveByUser(String userId) {
        return Optional.ofNullable(store.get(userId));
    }

@Override
public void saveNew(String userId, String incidentId, String channelId, Instant lastMessageAt) {
    var ctx = new ConversationContext(userId, incidentId, channelId, lastMessageAt, true);
    store.put(userId, ctx);
    log.info("Saved new context for {} -> {}", userId, ctx);
    log.debug("Current store: {}", store);
}

    @Override
    public void update(String userId, String incidentId, String channelId, Instant lastMessageAt) {
        ConversationContext ctx = new ConversationContext(userId, incidentId, channelId, lastMessageAt, true);
        store.put(userId, ctx);
        log.info("Updated context for user {} -> {}", userId, ctx);
        log.debug("Current store: {}", store);
    }
}





