package com.innovactions.incident.port.inbound;

import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;

public interface IncidentInboundPort {
    String reportIncident(CreateIncidentCommand command);
    void updateExistingIncident(CreateIncidentCommand command);
    void closeIncident(CloseIncidentCommand incidentCommand);
}
