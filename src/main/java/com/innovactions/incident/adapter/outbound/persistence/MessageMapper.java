package com.innovactions.incident.adapter.outbound.persistence;

import com.innovactions.incident.adapter.outbound.persistence.Entity.MessageEntity;
import com.innovactions.incident.domain.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public Message toDomain(MessageEntity entity) {
        return Message.loadExisting(
                entity.getId(),
                entity.getContent(),
                entity.getSentAt()
        );
    }
}
