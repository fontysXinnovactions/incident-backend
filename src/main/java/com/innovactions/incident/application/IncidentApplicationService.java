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
import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentApplicationService implements IncidentInboundPort {

    private final IncidentService incidentService;
    private final IncidentBroadcasterPort broadcaster;
    private final SeverityClassifierPort severityClassifier;
    private final IncidentClosurePort incidentClosurePort;
    private final List<IncidentReporterNotifierPort> reporterNotifiers;


    @Override
    public String handle(CreateIncidentCommand command) {
        //NOTE: Classify the severity of the incident report
        Severity severity = severityClassifier.classify(command.message());

        Incident incident = incidentService.createIncident(command, severity);

        //broadcast the incident to intended platform
        return broadcaster.broadcast(incident, command.platform());


    }

    @Override
    public void closeIncident(CloseIncidentCommand command) {

        String developerId = command.developerUserId();
        String channelId = command.channelId();
        String reason = command.reason();

        // 1️⃣ Close the Slack channel (always done)
        incidentClosurePort.closeIncident(developerId, channelId, reason);
        log.info("Closure... reporter '{}' via {}", developerId, channelId);

    }


    @Override
    public void handle(UpdateIncidentCommand command) {
        Incident updated = incidentService.updateIncident(command);
        broadcaster.updateBroadcast(updated, command.channelId());
    }
}
