package com.innovactions.incident.application;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextService {

    private final IncidentPersistencePort incidentPersistencePort;

  /**
   * Finds and updates a valid conversation context for a user.
   *
   * <p>Uses {@link #hasActiveContext(CreateIncidentCommand)} to check if the context Returns an
   * {@link UpdateIncidentCommand} if the context exists and is not expired.
   */
  @Transactional
  public UpdateIncidentCommand findValidUpdateContext(CreateIncidentCommand command) {

    var context = incidentPersistencePort.findById(command.reporterId());
    if (context.isEmpty()) {
      return null;
    }

      var ctx = context.get();
      var incident = ctx.incident();
      var channelId = ctx.slackChannelId();
    if (hasActiveContext(command)) {
        incidentPersistencePort.updateIncident(
                incident,
                command.message(),
                command.timestamp()
        );

      log.info("Updated context for user {}", incident);

      return UpdateIncidentCommand.builder()
          .channelId(channelId)
          .message(command.message())
          .updatedAt(command.timestamp())
          .build();
    }
    return null;
  }

//  // FIXME: decide the purpose of this method otherwise remove
//  public void saveNewIncident(Incident incident, String channelId) {
//    incidentRepository.saveNewIncident(incident, channelId);
//  }

  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
      Incident incident = new Incident(
              command.reporterId(),
              command.reporterName(),
              command.message(),
              Severity.MAJOR,
              "Developer"// or any default logic for now
      );
      incidentPersistencePort.saveNewIncident(incident, channelId);
  }

  /**
   * Checks if the user has an active conversation context. A context is active if it exists and is
   * not older than 24 hours.
   */
  public boolean hasActiveContext(CreateIncidentCommand command) {
    // Find user's active conversation (if any)
    var context = incidentPersistencePort.findById(command.reporterId());
    if (context.isEmpty()) {
      log.debug("No active context found for {}", command.reporterId());
      return false;
    }
    log.info("Incident found for reporter {}", command.reporterId());

    var ctx = context.get();
    // Check if the last message was more than 2 minutes
    boolean expired = command.timestamp().isAfter(ctx.incident().getReportedAt().plus(Duration.ofMinutes(2)));
    boolean active = !expired;
    log.debug("Active context for {} → {}", ctx.incident().getReporterId(), active);
    return active;
  }
    //FIXME: This is just for debugging
    public List<IncidentContext> getAllOpenIncidentByReporter(String reporterId) {
        Status status = Status.OPEN;
        return incidentPersistencePort.findByReporterAndStatus(reporterId, status);

    }
}
