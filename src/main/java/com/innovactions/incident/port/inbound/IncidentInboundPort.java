package com.innovactions.incident.port.inbound;

import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.application.command.CreateIncidentCommand;

public interface IncidentInboundPort {
  void reportIncident(CreateIncidentCommand command);

  void updateExistingIncident(CreateIncidentCommand command);

  void closeIncident(CloseIncidentCommand incidentCommand);
}
