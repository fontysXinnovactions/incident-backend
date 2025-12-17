package com.innovactions.incident.adapter.outbound.persistence.mapper;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.domain.model.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {
  public Incident toDomain(IncidentEntity entity) {

    Incident domain =
        new Incident(
            entity.getReporter().getReporterId(),
            entity.getReporter().getReporterId(),
            entity.getSummary(),
            entity.getSeverity(),
            "Assignee");
    // Override fields that exist on the domain
    domain.setStatus(entity.getStatus());
    return domain;
  }

  public IncidentEntity toEntity(Incident domain, String channelId) {
    if (domain == null) return null;

    return IncidentEntity.builder()
        .id(domain.getId())
        .summary(domain.getDetails())
        .severity(domain.getSeverity())
        .status(domain.getStatus())
        .createdAt(domain.getReportedAt())
        .slackChannelId(channelId)
        .build();
  }
}
