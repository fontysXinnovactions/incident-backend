package com.innovactions.incident.port.outbound;

public interface IncidentDetectorPort {
  boolean isIncident(String message);
}
