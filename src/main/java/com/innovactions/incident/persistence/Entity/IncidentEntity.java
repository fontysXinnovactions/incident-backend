package com.innovactions.incident.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_ref", referencedColumnName = "id")
    private ReporterEntity reporter;

    @Column(name = "details")
    private String content;

    @Column(name = "slack_channel_id")
    private String slackChannelId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

}
