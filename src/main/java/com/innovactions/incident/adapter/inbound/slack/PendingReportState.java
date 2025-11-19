package com.innovactions.incident.adapter.inbound.slack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Tracks pending incident reports per user to prevent duplicate reports.
 *
 * <p>Used with the Reporter Bot
 */
@Component
public class PendingReportState {

    private final Map<String, Boolean> userIdToPending = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userIdToUpdating = new ConcurrentHashMap<>();

  public void markPending(String userId) {
    userIdToPending.put(userId, Boolean.TRUE);
  }

  public boolean isPending(String userId) {
    return userIdToPending.containsKey(userId);
  }

    public void clearPending(String userId) {
        userIdToPending.remove(userId);
    }

    public void markUpdating(String userId) {
        userIdToUpdating.put(userId, Boolean.TRUE);
    }

    public boolean isUpdating(String userId) {
        return userIdToUpdating.containsKey(userId);
    }

    public void clearUpdating(String userId) {
        userIdToUpdating.remove(userId);
    }
}
