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

  /**
   * Handles a new incident report from the user.
   *
   * <p>Checks if an active conversation exists using . If active, delegates to {@link
   * #updateExistingIncident(CreateIncidentCommand)}; otherwise, creates and broadcasts a new
   * incident.
   */
  @Override
  public void reportIncident(CreateIncidentCommand command) {
    boolean updated = updateExistingIncident(command);
    if (updated) {
      return;
    }

    Severity severity = severityClassifier.classify(command.message());

    Incident incident = incidentService.createIncident(command, severity);
    String channelId = broadcaster.initSlackDeveloperWorkspace(incident, command.platform());
    conversationContextService.saveNewIncident(command, channelId, severity);
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
   * Use-case: Interpret messages from a conversation and determine if a message is an incident or
   * not.
   *
   * @param command Incoming incident
   * @return {@code true} if the incident was updated; {@code false} otherwise.
   */
  @Override
  public boolean updateExistingIncident(CreateIncidentCommand command) {

    UpdateIncidentCommand updateCommand =
        conversationContextService.findValidUpdateContext(command);

    // If it's not an update return
    if (updateCommand == null) {
      log.info(
          "No valid update context found for reporter {} — starting new incident flow.",
          command.reporterId());
      return false;
    }
    // If it's an update, update context and send it to the existing channel
    Incident updatedIncident = incidentService.updateIncident(updateCommand, command);
    broadcaster.updateIncidentToDeveloper(updatedIncident, updateCommand.channelId());
    return true;
  }
}
