package com.innovactions.incident.port.outbound;

import com.innovactions.incident.application.IncidentContext;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IncidentPersistencePort {

    void saveNewIncident(Incident incident, String channelId);//save new incident

    //find all active incidents of a reporter
    List<IncidentContext> findByReporterAndStatus(String reporterId, Status status);

    //persist a follow-up message for existing incident record
    void updateIncident(Incident incident, String followUpMessage, Instant timestamp);
    //FIXME: change to message

    //FIXME: this is ony to migrate in steps from old implementation which only return one context per reporter
    Optional<IncidentContext> findById(String reporterId);

}

//List<Incident> findActiveByReporter(String reporterRef);
//boolean existsByReporter(String reporterId);
