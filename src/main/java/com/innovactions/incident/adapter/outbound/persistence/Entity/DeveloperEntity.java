package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "developers")
@Data
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
