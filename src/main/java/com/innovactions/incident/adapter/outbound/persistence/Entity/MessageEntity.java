package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "incident_id", nullable = false)
  private IncidentEntity incident;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @Column(nullable = false)
  private Instant sentAt;

  @PrePersist
  private  void prePersist() {
    if (sentAt == null) sentAt = Instant.now();
  }
}
