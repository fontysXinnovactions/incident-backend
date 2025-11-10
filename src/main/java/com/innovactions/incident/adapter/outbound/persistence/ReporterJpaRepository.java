package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReporterJpaRepository extends JpaRepository<ReporterEntity, UUID> {
    Optional<ReporterEntity> findByReporterId(String reporterId);
}
