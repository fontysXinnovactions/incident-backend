package com.innovactions.incident.adapter.outbound.persistence.Repository;

import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessagesJpaRepository extends JpaRepository<MessageEntity, UUID> {}
