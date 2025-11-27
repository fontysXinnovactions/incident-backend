package com.innovactions.incident.application.listner;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.domain.event.IncidentClosedEvent;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentStatusUpdateListener {

    private final IncidentPersistencePort incidentPersistencePort;

    @Transactional
    @EventListener
    public void handleIncidentClosed(IncidentClosedEvent event) {

        log.info("Updating incident with Slack channel {} to RESOLVED",
                event.slackChannel());

        IncidentEntity incident =
                incidentPersistencePort.findBySlackChannelId(event.slackChannel())
                        .orElseThrow(() -> new IllegalStateException(
                                "No incident found for Slack channel " + event.slackChannel()));

        incidentPersistencePort.updateIncidentStatus(incident.getId().toString(),
                Status.RESOLVED);

        log.info("Incident {} marked RESOLVED", incident.getId());
    }
}
