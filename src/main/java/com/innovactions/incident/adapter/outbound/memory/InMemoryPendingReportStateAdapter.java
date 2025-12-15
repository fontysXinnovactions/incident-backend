package com.innovactions.incident.adapter.outbound.memory;

import com.innovactions.incident.port.outbound.PendingReportStatePort;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryPendingReportStateAdapter implements PendingReportStatePort {
  private final Map<String, Boolean> pending = new ConcurrentHashMap<>();
  private final Map<String, Boolean> updating = new ConcurrentHashMap<>();

  @Override
  public void markPending(String userId) {
    pending.put(userId, Boolean.TRUE);
  }

  @Override
  public boolean isPending(String userId) {
    return pending.containsKey(userId);
  }

  @Override
  public void clearPending(String userId) {
    pending.remove(userId);
  }

  @Override
  public void markUpdating(String userId) {
    updating.put(userId, Boolean.TRUE);
  }

  @Override
  public boolean isUpdating(String userId) {
    return updating.containsKey(userId);
  }

  @Override
  public void clearUpdating(String userId) {
    updating.remove(userId);
  }
}
