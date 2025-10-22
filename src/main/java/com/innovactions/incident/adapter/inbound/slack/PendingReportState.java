package com.innovactions.incident.adapter.inbound.slack;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Tracks pending incident reports per user to prevent duplicate reports.</p>
 * <p>Used with the Reporter Bot</p>
 */
@Component
public class PendingReportState {

    private final Map<String, Boolean> userIdToPending = new ConcurrentHashMap<>();

    public void markPending(String userId) {
        userIdToPending.put(userId, Boolean.TRUE);
    }

    public boolean isPending(String userId) {
        return userIdToPending.containsKey(userId);
    }

    public void clear(String userId) {
        userIdToPending.remove(userId);
    }
}


