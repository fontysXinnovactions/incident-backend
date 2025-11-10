package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, UUID> {
    Optional<IncidentEntity> findActiveByReporter_ReporterId(String reporterId);
    // Check existence by encrypted reporter ID
    boolean existsByReporter_ReporterId(String reporterId);
}
