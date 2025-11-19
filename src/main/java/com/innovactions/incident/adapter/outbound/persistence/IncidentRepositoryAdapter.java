package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.outbound.IncidentRepositoryPort;

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
public class IncidentRepositoryAdapter implements IncidentRepositoryPort {
  private final IncidentJpaRepository incidentJpaRepository;
  private final ReporterJpaRepository reporterJpaRepository;
  private final MessagesJpaRepository messagesJpaRepository;
  private final EncryptionAdapter encryptionAdapter;

  @Transactional
  @Override
  public void saveNewIncident(CreateIncidentCommand command, String channelId) {
    String encryptedReporterId = encryptionAdapter.encrypt(command.reporterId());
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
                .summary(command.message())
            .slackChannelId(channelId)
            .createdAt(command.timestamp())
            .reporter(reporterEntity)
            .build();

    incidentJpaRepository.save(incidentEntity);

    MessageEntity messageEntity =
        MessageEntity.builder()
            .incident(incidentEntity)
            .content(command.message())
            .sentAt(command.timestamp())
            .build();

    messagesJpaRepository.save(messageEntity);

    log.info("Incident persisted for reporter {}", command.reporterId());
  }

  @Override
  public Optional<IncidentEntity> findById(String reporterId) {
    String encrypted = encryptionAdapter.encrypt(reporterId);
    return incidentJpaRepository.findActiveByReporter_ReporterId(encrypted);
  }

  @Override
  public List<IncidentEntity> findActiveByReporter(String reporterRef) {
    return List.of();
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
