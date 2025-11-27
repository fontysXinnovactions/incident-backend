package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "developers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "slack_id")
  private String slackId;
}
