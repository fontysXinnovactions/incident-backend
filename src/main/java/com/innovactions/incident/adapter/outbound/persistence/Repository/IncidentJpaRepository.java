package com.innovactions.incident.adapter.outbound.persistence.Repository;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.innovactions.incident.application.IncidentContext;
import com.innovactions.incident.domain.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, UUID> {
  Optional<IncidentEntity> findActiveByReporter_ReporterId(String reporterId);
// Optional<IncidentEntity> findByReporter_ReporterIdAndStatus(String reporterId, Status status);


    // Check existence by encrypted reporter ID
  boolean existsByReporter_ReporterId(String reporterId);

  List<IncidentEntity> findAllByReporter_ReporterIdAndStatus(String reporterId, Status status);

}
