package com.innovactions.incident.domain.service;

import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IncidentService {
  public Incident createIncident(
      String reporterId, String reporter, String message, Severity severity) {
    String assignee = assign(message);

    Incident incident = new Incident(reporterId, reporter, message, severity, assignee);

    log.info("Created new incident: {}", incident.getId());
    return incident;
  }

  public Incident updateIncident(Incident existing, String newMessage, Instant updatedAt) {
    // FIXME: decide on the Update incident command usage
    // NOTE: the parameters are used to temporarily break the reverse dependency
    existing.updateDetails(newMessage);
//    existing(updatedAt);
    log.info("Updated incident {} at {}", existing.getId(), updatedAt);
    return existing;

  }

  //  public Incident updateIncident(UpdateIncidentCommand command) {
  //    // TODO: Refactor test only
  //    Incident updated =
  //        new Incident(
  //            command.channelId(), // keep the same ID
  //            "ReporterName", // TODO: preserve from original incident
  //            command.message(),
  //            Severity.MINOR, // TODO: decide whether to reclassify
  //            "Bob" // TODO: preserve original assignee
  //            );
  //
  //    log.info(
  //        "Updated incident {} with new message at {}", command.channelId(), command.updatedAt());
  //
  //    return updated;
  //  }

  private String assign(String message) {
    return "Bob";
  }
}
