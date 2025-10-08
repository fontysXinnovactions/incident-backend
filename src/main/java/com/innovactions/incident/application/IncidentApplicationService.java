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
    public String handleNewIncident(CreateIncidentCommand command) {

        if (command.platform() == Platform.WHATSAPP) {
            // check context if true or false
        }
        //NOTE: Classify the severity of the incident report
        Severity severity = severityClassifier.classify(command.message());

        Incident incident = incidentService.createIncident(command, severity);


        //broadcast the incident to intended platform
        String channelId = broadcaster.broadcast(incident, command.platform());

        // ifcontext returned true then update context

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


    /**
     * Use-case: Interpret messages from a conversation
     * and determine if a message is an incident or not.
     *<p>
     * Example:
     *  Message=<b>"Good morning" </b>=> not an incident
     *<p>
     *  Message="Hey Bob I can't login to my front end" => an incident
     *
     * @param command Incoming incident
     */
    @Override
    public void handlePossibleIncident(UpdateIncidentCommand command) {
        Incident updated = incidentService.updateIncident(command);
        broadcaster.updateBroadcast(updated, command.channelId());
    }
}
