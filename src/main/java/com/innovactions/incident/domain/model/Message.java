package com.innovactions.incident.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Message {

    private final UUID id;
    private final String content;
    private final Instant sentAt;

    public static Message createNew(String content, Instant sentAt) {
        return new Message(null, content, sentAt);  // id assigned later by DB
    }
    public static Message loadExisting(UUID id, String content, Instant sentAt) {
        return new Message(id, content, sentAt);
    }
}
