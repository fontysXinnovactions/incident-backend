package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import com.innovactions.incident.adapter.outbound.persistence.Repository.IncidentJpaRepository;
import com.innovactions.incident.adapter.outbound.persistence.Repository.MessagesJpaRepository;
import com.innovactions.incident.adapter.outbound.persistence.Repository.ReporterJpaRepository;
import com.innovactions.incident.adapter.outbound.persistence.mapper.IncidentMapper;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.IncidentContext;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
  private final IncidentMapper incidentMapper;

    /**
     * Usecase: save new incident
     *
     * A reporter can have one or more incidents
     * @param incident
     * @param channelId
     */
  @Transactional
  @Override
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
//                .summary(incident.getMessages().stream()
//                        .map(m -> m.getContent())
//                        .collect(Collectors.joining(" ")))//Fixme: this should save AI summary
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
            .content(incident.getAiSummary())//should be details for testing only
            .sentAt(incident.getReportedAt())
            .build();

    messagesJpaRepository.save(messageEntity);

    log.info("Incident persisted for reporter {}", incident.getReporterId());
  }


    /**
     * Finds all incidents for the given reporter with the specified status.
     *
     * @param reporterId reporter's plain (unencrypted) ID
     * @param status     incident status to filter by
     * @return list of IncidentContext containing the incident and its Slack channel ID
     */
    @Override
    public List<IncidentContext> findByReporterAndStatus(String reporterId, Status status) {
        String encrypted = encryptionAdapter.encrypt(reporterId);
        return incidentJpaRepository
                .findAllByReporter_ReporterIdAndStatus(encrypted, status)
                .stream()
                .map(entity -> new IncidentContext(
                        incidentMapper.toDomain(entity),
                        entity.getSlackChannelId()
                ))
                .toList();
    }

  @Transactional
  @Override
  public void updateIncident(Incident incident, String followUpMessage, Instant timestamp) {
//TODO: Maybe remove not a good idea to create a new entity

      MessageEntity messageEntity =
        MessageEntity.builder()
            .incident(incidentMapper.toEntity(incident))
            .content(followUpMessage)
            .sentAt(timestamp)
            .build();

    messagesJpaRepository.save(messageEntity);
  }

    @Override
    public Optional<IncidentContext> findById(String reporterId) {
        String encrypted = encryptionAdapter.encrypt(reporterId);

        return incidentJpaRepository
                .findActiveByReporter_ReporterId(encrypted)
                .map(entity ->
                        new IncidentContext(
                                incidentMapper.toDomain(entity),
                                entity.getSlackChannelId()  // stays in database layer
                        )
                );

    }

//    @Override
//    public boolean existsByReporter(String reporterId) {
//        String encryptedReporterId = encryptionAdapter.encrypt(reporterId);
//        return incidentJpaRepository.existsByReporter_ReporterId(encryptedReporterId);
//    }

//    @Override
//    public List<Incident> findActiveByReporter(String reporterRef) {
//
//        String encrypted = encryptionAdapter.encrypt(reporterRef);
//
//        return incidentJpaRepository
//                .findAllByReporter_ReporterIdAndStatus(encrypted, Status.OPEN)   // or another query method
//                .stream()
//                .map(incidentMapper::toDomain)
//                .toList();
//    }

}
