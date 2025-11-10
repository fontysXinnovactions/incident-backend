package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reporters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reporter_id", unique = true, nullable = false)
    private String reporterId;
}
