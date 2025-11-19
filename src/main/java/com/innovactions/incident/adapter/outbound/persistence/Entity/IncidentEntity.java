package com.innovactions.incident.adapter.outbound.persistence.Entity;

import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.model.Status;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "summary")
  private String summary;

  @Column(name = "slack_channel_id")
  private String slackChannelId;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private Status status;

  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_ref", referencedColumnName = "id")
  private ReporterEntity reporter;
}
