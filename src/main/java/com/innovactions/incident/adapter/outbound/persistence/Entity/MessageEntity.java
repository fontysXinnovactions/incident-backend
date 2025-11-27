package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "incident_ref", nullable = false)
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
