package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "dispatch")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchEntity {

  @EmbeddedId private DispatchId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("developerId")
  @JoinColumn(name = "developer_id", nullable = false)
  private DeveloperEntity developer;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("incidentId")
  @JoinColumn(name = "incident_id", nullable = false)
  private IncidentEntity incident;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  public DispatchEntity(DeveloperEntity developer, IncidentEntity incident, Instant assignedAt) {
    this.developer = developer;
    this.incident = incident;
    this.assignedAt = assignedAt;
    this.id = new DispatchId(developer.getId(), incident.getId());
  }

  public static DispatchEntity of(
      DeveloperEntity developer, IncidentEntity incident, Instant assignedAt) {
    return new DispatchEntity(developer, incident, assignedAt);
  }
}
