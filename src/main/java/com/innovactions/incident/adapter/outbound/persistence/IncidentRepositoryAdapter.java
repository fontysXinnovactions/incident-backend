package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.outbound.IncidentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentRepositoryAdapter implements IncidentRepositoryPort {
    private final IncidentJpaRepository incidentJpaRepository;
    private final ReporterJpaRepository reporterJpaRepository;
    private final EncryptionAdapter encryptionAdapter;

    @Override
    public void saveNewIncident(CreateIncidentCommand command, String channelId) {
        String encryptedReporterId = encryptionAdapter.encrypt(command.reporterId());
        // checks if reporter exists before adding new  reporter
        ReporterEntity reporterEntity = reporterJpaRepository.findByReporterId(encryptedReporterId)
                .orElseGet(() -> {
                    ReporterEntity newReporter = ReporterEntity.builder()
                            .reporterId(encryptedReporterId)
                            .build();
                    return reporterJpaRepository.save(newReporter);
                });

        IncidentEntity incidentEntity = IncidentEntity.builder()
                .content(command.message())
                .slackChannelId(channelId)
                .createdAt(command.timestamp())
                .reporter(reporterEntity)
                .build();

        incidentJpaRepository.save(incidentEntity);
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

    @Override
    public IncidentEntity updateIncident(IncidentEntity incident) {
        return null;
    }
}
