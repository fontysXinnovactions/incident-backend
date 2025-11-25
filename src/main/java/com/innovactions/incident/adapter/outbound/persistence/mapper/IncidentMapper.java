package com.innovactions.incident.adapter.outbound.persistence.mapper;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.domain.model.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {
    public Incident toDomain(IncidentEntity entity) {

        Incident domain = new Incident(
                entity.getReporter().getReporterId(),
                entity.getReporter().getReporterId(),//FIXME:ADD name
                entity.getSummary(), //FIXME:This might have to be details
                entity.getSeverity(),
                "Assignee"
        );
        // Override fields that exist on the domain
        domain.setStatus(entity.getStatus());
        domain.setAiSummary(entity.getSummary());
        return domain;
    }
    public IncidentEntity toEntity(Incident domain) {
        if (domain == null) return null;

        return IncidentEntity.builder()
                .summary(domain.getAiSummary())
                .severity(domain.getSeverity())
                .status(domain.getStatus())
                .slackChannelId(null)               // set by application logic
                .createdAt(domain.getReportedAt())  // domain source of truth
                .build();
    }

}
