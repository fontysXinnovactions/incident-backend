package com.innovactions.incident.persistence;

import com.innovactions.incident.domain.model.ConversationContext;
import com.innovactions.incident.persistence.Entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {
//    Optional<ConversationContext> findActiveByReporterId(String reporterId);
}
