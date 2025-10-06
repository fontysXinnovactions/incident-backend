package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.Severity;

public interface IncidentSeverityClassifierPort {
    Severity classifyIncident(String message);
}
