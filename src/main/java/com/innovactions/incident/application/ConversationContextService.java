package com.innovactions.incident.application;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.adapter.outbound.persistence.MessagesJpaRepository;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextService {



  private final IncidentPersistencePort incidentPersistencePort;
  private final MessagesJpaRepository messagesJpaRepository; // FIXME: move everything got to do with jpa to the repository adapter

  /**
   * Finds and updates a valid conversation context for a user.
   *
   * <p>Uses {@link #hasActiveContext(CreateIncidentCommand)} to check if the context Returns an
   * {@link UpdateIncidentCommand} if the context exists and is not expired.
   */
  @Transactional
  public UpdateIncidentCommand findValidUpdateContext(CreateIncidentCommand command) {
      //Step 1 - reporter should have at least one open incident
      if (!hasActiveContext(command)) {
          return null;
      }

      // Step 2 - fetch all open incident from the reporter
      var incidents = incidentPersistencePort.findAllActiveByReporter(command.reporterId());

      // TODO: Steps 3 and 4 should eventually be replaced by AI selecting the correct conversation.
      // NOTE: Match the appropriate incident by comparing persisted aiSummary values or message history.
      //Step 3 - Get the latest Incident from the user
      var latest = incidents.stream()
              .max(Comparator.comparing(IncidentEntity::getCreatedAt))
              .get();

     // Step 4 - Check expiration (5 minutes window)
      boolean expired = command.timestamp()
              .isAfter(latest.getCreatedAt().plus(Duration.ofMinutes(5)));

      if (expired) {
          log.info("Latest incident expired for reporter {}, new incident required",
                  command.reporterId());
          return null;
      }
      //Step 5 - Update incident add follow-up message
      MessageEntity messageEntity =
              MessageEntity.builder()
                      .incident(latest)
                      .content(command.message())
                      .sentAt(command.timestamp())
                      .build();

      messagesJpaRepository.save(messageEntity);
      log.info("Updated context for user {}", latest);

      return UpdateIncidentCommand.builder()
              .channelId(latest.getSlackChannelId())
              .message(command.message())
              .updatedAt(command.timestamp())
              .build();


  }

  // FIXME: decide the purpose of this method otherwise remove
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
      // return light boolean row check
      // do not pull all list and filter
      // add status as well
      boolean exists = incidentPersistencePort.existsByReporter(command.reporterId());

      if (!exists) {
          log.debug("No active context found for {}", command.reporterId());
          return false;
      }

      log.info("Reporter {} DOES have a previous incident", command.reporterId());
      return true;
  }

}

