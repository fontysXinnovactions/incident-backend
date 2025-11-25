package com.innovactions.incident.adapter.outbound.persistence.Repository;

import com.innovactions.incident.adapter.outbound.persistence.Entity.ReporterEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporterJpaRepository extends JpaRepository<ReporterEntity, UUID> {
  Optional<ReporterEntity> findByReporterId(String reporterId);
}
