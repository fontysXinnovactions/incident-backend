package com.innovactions.incident.port.outbound;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.application.command.CreateIncidentCommand;

import java.util.List;
import java.util.Optional;

public interface IncidentRepositoryPort {
    void saveNewIncident(CreateIncidentCommand command, String channelId);
    Optional<IncidentEntity> findById(String reporterId);
    List<IncidentEntity> findActiveByReporter(String reporterRef);
    boolean existsByReporter(String reporterId);
    IncidentEntity updateIncident(IncidentEntity incident);
//    Incident saveNewIncident(Incident incident);
//    Optional<Incident> findById(UUID id);
//
//    List<Incident> findActiveByReporter(String reporterRef);
//    boolean existsByReporter(String reporterId);
//
//    Incident updateIncident(Incident incident);
}
