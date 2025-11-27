package com.innovactions.incident.adapter.outbound.persistence.mapper;

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

    public MessageEntity toEntity(Message domain) {
        if (domain == null) return null;

        return MessageEntity.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .sentAt(domain.getSentAt())
                .build();//Fixme: Add entity?
    }
}
