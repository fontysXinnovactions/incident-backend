package com.innovactions.incident.domain.service;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IncidentService {

  public Incident createIncident(CreateIncidentCommand command, Severity severity) {
    String assignee = assign(command.message());

    Incident incident =
        new Incident(
            command.reporterId(), command.reporterName(), command.message(), severity, assignee);

    log.info("Created new incident: {}", incident.getId());
    return incident;
  }

    public Incident updateExistingIncident(UpdateIncidentCommand command, CreateIncidentCommand createCommand) {
        String assignee = assign(command.message());

        Incident updated = new Incident(
                createCommand.reporterId(),
                createCommand.reporterName(),
                command.message(),
                Severity.MINOR,            // TODO: decide whether to reclassify
                assignee
        );

    log.info(
        "Updated incident {} with new message at {}", command.channelId(), command.updatedAt());

    return updated;
  }

  private String assign(String message) {
    return "Bob";
  }
}
