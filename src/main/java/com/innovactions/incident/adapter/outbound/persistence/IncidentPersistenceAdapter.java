package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import com.innovactions.incident.adapter.outbound.persistence.mapper.IncidentMapper;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                        .id(incident.getId())
                        .summary(incident.getDetails())
                        .slackChannelId(channelId)
                        .severity(incident.getSeverity())
                        .status(incident.getStatus())
                        .createdAt(incident.getReportedAt())
                        .reporter(reporterEntity)
                        .build();
        //FIXME: check the behaviour with list of messages
        IncidentEntity savedIncident = incidentJpaRepository.saveAndFlush(incidentEntity);

        MessageEntity messageEntity =
                MessageEntity.builder()
                        .incident(savedIncident)
                        .content(incident.getDetails())
                        .sentAt(incident.getReportedAt())
                        .build();

        messagesJpaRepository.save(messageEntity);

        log.info("Incident persisted for reporter {}", incident.getReporterId());
    }


    /**
     * Finds all incidents for the given reporter with the specified status.
     *
     * @param reporterId reporter's plain (unencrypted) ID
     * @return list of IncidentContext containing the incident and its Slack channel ID
     */
    @Override
    public List<IncidentEntity> findAllActiveByReporter(String reporterId, Status status) {
              String encrypted = encryptionAdapter.encrypt(reporterId);
        return incidentJpaRepository
                .findAllByReporter_ReporterIdAndStatus(encrypted, status);

    }

    @Override
    public boolean existsByReporter(String reporterId, Status status) {
        String encryptedReporterId = encryptionAdapter.encrypt(reporterId);
        return incidentJpaRepository.existsByReporter_ReporterIdAndStatus(encryptedReporterId, status );
    }

    @Override
    public Optional<IncidentEntity> findBySlackChannelId(String slackChannelId) {
        return incidentJpaRepository.findBySlackChannelId(slackChannelId);
    }

    /**
     * Updates the status of an incident
     * Currently used for incident marked as resolved
     * @param incidentId
     * @param newStatus
     */
    @Override
    public void updateIncidentStatus(String incidentId, Status newStatus) {
        IncidentEntity incident = incidentJpaRepository.findById(UUID.fromString(incidentId))
                .orElseThrow(() -> new IllegalStateException("Incident not found: " + incidentId));

        incident.setStatus(newStatus);
        incidentJpaRepository.save(incident);
    }

}
