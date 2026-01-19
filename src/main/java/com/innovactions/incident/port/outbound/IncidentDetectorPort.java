package com.innovactions.incident.port.outbound;

public interface IncidentDetectorPort {
  boolean isValidIncident(String message);
}
