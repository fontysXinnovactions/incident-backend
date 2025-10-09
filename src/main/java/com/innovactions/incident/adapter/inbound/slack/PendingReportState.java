package com.innovactions.incident.adapter.inbound.slack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

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
