package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.IncidentClassification;

public interface SeverityClassifierPort {
  IncidentClassification classify(String message);
}
