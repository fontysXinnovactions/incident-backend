package com.innovactions.incident.persistence;

import com.innovactions.incident.persistence.Entity.ReporterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReporterRepository extends JpaRepository<ReporterEntity, UUID> {
}
