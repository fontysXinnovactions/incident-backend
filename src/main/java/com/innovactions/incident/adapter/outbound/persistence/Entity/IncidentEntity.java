package com.innovactions.incident.adapter.outbound.persistence.Entity;

import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.domain.model.Status;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {
  //    @GeneratedValue(strategy = GenerationType.UUID)
  @Id private UUID id;

  @Column(name = "summary")
  private String summary;

  @Column(name = "slack_channel_id")
  private String slackChannelId;

  @Enumerated(EnumType.STRING)
  private Severity severity;

  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(name = "assignee")
  private String assignee;

  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", referencedColumnName = "id")
  private ReporterEntity reporter;

  @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MessageEntity> messages = new ArrayList<>();
}
