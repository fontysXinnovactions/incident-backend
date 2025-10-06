package com.innovactions.incident.application;

import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.service.IncidentService;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.IncidentBroadcasterPort;
import com.innovactions.incident.port.outbound.IncidentClosurePort;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentApplicationService implements IncidentInboundPort {

    private final IncidentService incidentService;
    private final IncidentBroadcasterPort broadcaster;
    private final SeverityClassifierPort severityClassifier;
    private final IncidentClosurePort incidentClosurePort;

    @Override
    public String handle(CreateIncidentCommand command) {
        Severity severity = severityClassifier.classify(command.message());

        Incident incident = incidentService.createIncident(command, severity);

        return broadcaster.broadcast(incident, command.platform());//TODO: look for better ways to do it
//        return broadcaster.broadcast(incident);//TODO: look for better ways to do it

    }

    @Override
    public void closeIncident(CloseIncidentCommand incidentCommand) {
        incidentClosurePort.closeIncident(incidentCommand.developerUserId(), incidentCommand.channelId(), incidentCommand.reason());
    }

    @Override
    public void handle(UpdateIncidentCommand command) {
        Incident updated = incidentService.updateIncident(command);
        broadcaster.updateBroadcast(updated, command.channelId());
    }
}
