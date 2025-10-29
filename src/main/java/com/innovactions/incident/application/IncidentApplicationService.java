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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentApplicationService implements IncidentInboundPort {

    private final IncidentService incidentService;
    private final IncidentBroadcasterPort broadcaster;
    private final SeverityClassifierPort severityClassifier;
    private final IncidentClosurePort incidentClosurePort;
    private final ConversationContextService conversationContextService;


    @Override
    public String reportIncident(CreateIncidentCommand command) {
        Severity severity = severityClassifier.classify(command.message());

        Incident incident = incidentService.createIncident(command, severity);

        return broadcaster.initSlackDeveloperWorkspace(incident, command.platform());
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
     *
     * @param command Incoming incident
     */
    @Override
    public void updateIncident(CreateIncidentCommand command) {
        if (!isIncident(command)) return;

        UpdateIncidentCommand updateCommand = conversationContextService.isNewOrExpired(command);

        // If it's not an update, we simply create a new one and save it to context
        if (updateCommand == null) {
            String channelId = reportIncident(command);
            conversationContextService.saveNewIncident(command, channelId);
            return;
        }
        // If it's an update, we update context and send it to the existing channel
        Incident updatedIncident = incidentService.updateIncident(updateCommand);
        broadcaster.updateIncidentToDeveloper(updatedIncident, updateCommand.channelId());
    }

    /**
     * DUMMY HELPER FUNCTION FAKING INCIDENT DETERMINATION
     * TODO: MAKE INTELLIGENT INCIDENT DETERMINATION
     */
    private boolean isIncident(CreateIncidentCommand command) {
        String[] keywords = {"down", "failure", "fail", "broken", "bad"};
        for (String keyword : keywords) {
            if (command.message().toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
