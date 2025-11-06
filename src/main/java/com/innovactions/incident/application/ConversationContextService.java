package com.innovactions.incident.application;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.service.EncryptionService;
import com.innovactions.incident.persistence.Entity.IncidentEntity;
import com.innovactions.incident.persistence.Entity.ReporterEntity;
import com.innovactions.incident.persistence.IncidentRepository;
import com.innovactions.incident.persistence.ReporterRepository;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextService {

  private final ConversationRepositoryPort conversationRepository;
  private final IncidentRepository incidentRepository;
  private final ReporterRepository reporterRepository;
  private final EncryptionService encryptionService;

  /**
   * Finds and updates a valid conversation context for a user.
   *
   * <p>Uses {@link #hasActiveContext(CreateIncidentCommand)} to check if the context Returns an
   * {@link UpdateIncidentCommand} if the context exists and is not expired.
   */
     public UpdateIncidentCommand findValidUpdateContext(CreateIncidentCommand command) {
    var context = conversationRepository.findActiveByUser(command.reporterId());
    if (context.isEmpty()) {
      return null;
    }
    var ctx = context.get();
    if (hasActiveContext(command)) {

      conversationRepository.update(
          ctx.userId(), ctx.incidentId(), ctx.channelId(), command.timestamp());

      return UpdateIncidentCommand.builder()
          .channelId(context.get().channelId())
          .message(command.message())
          .updatedAt(command.timestamp())
          .build();
    }
    return null;
  }
  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
      ReporterEntity reporterEntity = ReporterEntity.builder()
              .reporterId(encryptionService.encrypt(command.reporterId()))
              .build();
      reporterRepository.save(reporterEntity);

      IncidentEntity incidentEntity = IncidentEntity.builder()
              .content(command.message())
              .slackChannelId(channelId)
              .createdAt(command.timestamp())
              .reporter(reporterEntity)
              .build();

      incidentRepository.save(incidentEntity);
      log.info("Incident persisted for reporter {}", command.reporterId());
  }
//  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
//    conversationRepository.saveNew(
//        command.reporterId(), "INCIDENT-" + command.timestamp(), channelId, command.timestamp());
//  }

  /**
   * Checks if the user has an active conversation context. A context is active if it exists and is
   * not older than 24 hours.
   */
  public boolean hasActiveContext(CreateIncidentCommand command) {
    // Find user's active conversation (if any)
    var context = conversationRepository.findActiveByUser(command.reporterId());
//    var context = incidentRepository.findActiveByReporterId(command.reporterId());

    if (context.isEmpty()) {
      log.debug("No active context found for {}", command.reporterId());
      return false;
    }

    var ctx = context.get();
    // Check if the last message was more than 24 hours ago
    boolean expired = command.timestamp().isAfter(ctx.lastMessageAt().plus(Duration.ofHours(24)));

    boolean active = !expired;
//    log.debug("Active context for {} → {}", ctx.userId(), active);
    return active;
  }
}
