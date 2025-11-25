package com.innovactions.incident.port.outbound;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IncidentPersistencePort {
  void saveNewIncident(Incident incident, String channelId);

  Optional<IncidentEntity> findById(String reporterId);

  List<IncidentEntity> findActiveByReporter(String reporterRef);

  boolean existsByReporter(String reporterId);

  void updateIncident(IncidentEntity incident, String followUpMessage,
                                Instant timestamp);

}
