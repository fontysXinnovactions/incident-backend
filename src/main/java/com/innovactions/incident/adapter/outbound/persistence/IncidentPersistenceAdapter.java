package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import com.innovactions.incident.adapter.outbound.persistence.mapper.IncidentMapper;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentPersistenceAdapter implements IncidentPersistencePort {
  private final IncidentJpaRepository incidentJpaRepository;
  private final ReporterJpaRepository reporterJpaRepository;
  private final MessagesJpaRepository messagesJpaRepository;
  private final EncryptionAdapter encryptionAdapter;
//  private final IncidentMapper incidentMapper;

  @Transactional
  @Override
//  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
  public void saveNewIncident(Incident incident, String channelId) {
    String encryptedReporterId = encryptionAdapter.encrypt(incident.getReporterId());
    // checks if reporter exists before adding new  reporter
    ReporterEntity reporterEntity =
        reporterJpaRepository
            .findByReporterId(encryptedReporterId)
            .orElseGet(
                () -> {
                  ReporterEntity newReporter =
                      ReporterEntity.builder().reporterId(encryptedReporterId).build();
                  return reporterJpaRepository.save(newReporter);
                });

    IncidentEntity incidentEntity =
        IncidentEntity.builder()
                .summary(incident.getDetails())
            .slackChannelId(channelId)
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .createdAt(incident.getReportedAt())
            .reporter(reporterEntity)
            .build();

    incidentJpaRepository.save(incidentEntity);

    MessageEntity messageEntity =
        MessageEntity.builder()
            .incident(incidentEntity)
                .content(incident.getDetails())
                .sentAt(incident.getReportedAt())
            .build();

    messagesJpaRepository.save(messageEntity);

    log.info("Incident persisted for reporter {}", incident.getReporterId());
  }

  @Override
  public Optional<IncidentEntity> findById(String reporterId) {
    String encrypted = encryptionAdapter.encrypt(reporterId);
    return incidentJpaRepository.findActiveByReporter_ReporterId(encrypted);
  }

    /**
     * Finds all incidents for the given reporter with the specified status.
     *
     * @param reporterId reporter's plain (unencrypted) ID
     * @return list of IncidentContext containing the incident and its Slack channel ID
     */
  @Override
  public List<IncidentEntity> findAllActiveByReporter(String reporterId) {
      String encrypted = encryptionAdapter.encrypt(reporterId);
      return incidentJpaRepository
              .findAllByReporter_ReporterId(encrypted);
  }

  @Override
  public boolean existsByReporter(String reporterId) {
    String encryptedReporterId = encryptionAdapter.encrypt(reporterId);
    return incidentJpaRepository.existsByReporter_ReporterId(encryptedReporterId);
  }

  @Transactional
  @Override
  public void updateIncident(IncidentEntity incident,
                             String followUpMessage,
                             Instant timestamp) {

      MessageEntity messageEntity = MessageEntity.builder()
              .incident(incident)
              .content(followUpMessage)
              .sentAt(timestamp)
              .build();

      messagesJpaRepository.save(messageEntity);

  }
}
