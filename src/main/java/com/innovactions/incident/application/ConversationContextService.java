package com.innovactions.incident.application;

import com.innovactions.incident.adapter.outbound.persistence.IncidentJpaRepository;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.port.outbound.IncidentRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationContextService {


  private final IncidentJpaRepository incidentJpaRepositoryPort;
  private final IncidentRepositoryPort incidentRepository;
  /**
   * Finds and updates a valid conversation context for a user.
   *
   * <p>Uses {@link #hasActiveContext(CreateIncidentCommand)} to check if the context Returns an
   * {@link UpdateIncidentCommand} if the context exists and is not expired.
   */
  @Transactional
  public UpdateIncidentCommand findValidUpdateContext(CreateIncidentCommand command) {

      var context = incidentRepository.findById(command.reporterId());
      if (context.isEmpty()) {
          return null;
      }
      var ctx = context.get();
      if (hasActiveContext(command)) {

          ctx.setContent(command.message());
          ctx.setCreatedAt(command.timestamp());
          incidentJpaRepositoryPort.save(ctx);
          log.info("Updated context for user {}", ctx);

          return UpdateIncidentCommand.builder()
                  .channelId(context.get().getSlackChannelId())
                  .message(command.message())
                  .updatedAt(command.timestamp())
                  .build();
      }
      return null;
  }
//FIXME: decide the purpose of this method otherwise remove
  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
      incidentRepository.saveNewIncident(command, channelId);
  }

  /**
   * Checks if the user has an active conversation context. A context is active if it exists and is
   * not older than 24 hours.
   */
  public boolean hasActiveContext(CreateIncidentCommand command) {
    // Find user's active conversation (if any)
    var context = incidentRepository.findById(command.reporterId());
    if (context.isEmpty()) {
      log.debug("No active context found for {}", command.reporterId());
      return false;
    }
    log.info("Incident found for reporter {}", command.reporterId());

    var ctx = context.get();
    // Check if the last message was more than 24 hours ago
    // boolean expired = command.timestamp().isAfter(ctx.getCreatedAt().plus(Duration.ofHours(24)));
    boolean expired = command.timestamp().isAfter(ctx.getCreatedAt().plus(Duration.ofMinutes(2)));
    boolean active = !expired;
    log.debug("Active context for {} → {}", ctx.getReporter(), active);
    return active;
  }
}
