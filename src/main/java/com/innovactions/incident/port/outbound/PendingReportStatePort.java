package com.innovactions.incident.port.outbound;

public interface PendingReportStatePort {
  void markPending(String userId);

  boolean isPending(String userId);

  void clearPending(String userId);

  void markUpdating(String userId);

  boolean isUpdating(String userId);

  void clearUpdating(String userId);
}
