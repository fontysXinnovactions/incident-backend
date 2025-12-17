package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.domain.model.Status;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, UUID> {

  Optional<IncidentEntity> findBySlackChannelId(String slackChannelId);

  List<IncidentEntity> findAllByReporter_ReporterIdAndStatus(String reporterId, Status status);

  boolean existsByReporter_ReporterIdAndStatus(String reporterId, Status status);

  List<IncidentEntity> findAllByStatus(Status status, Pageable pageable);
}
