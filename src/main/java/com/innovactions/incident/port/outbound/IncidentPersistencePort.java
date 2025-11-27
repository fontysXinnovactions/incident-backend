package com.innovactions.incident.port.outbound;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Status;

import java.util.List;
import java.util.Optional;

public interface IncidentPersistencePort {
  void saveNewIncident(Incident incident, String channelId);

  List<IncidentEntity> findAllActiveByReporter(String reporterId, Status status);

  boolean existsByReporter(String reporterId, Status status);

  // needed for resolving incidents via Slack channel
  Optional<IncidentEntity> findBySlackChannelId(String slackChannelId);

  // update status
  void updateIncidentStatus(String incidentId, Status newStatus);



}
