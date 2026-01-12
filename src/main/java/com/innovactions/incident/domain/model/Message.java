package com.innovactions.incident.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Message {

  private final UUID id;
  private final String content;
  private final Instant sentAt;
  private final String platformMessageId;

  public static Message createNew(String content, Instant sentAt, String platformMessageId) {
    return new Message(null, content, sentAt, platformMessageId); // id assigned later by DB
  }

  public static Message loadExisting(UUID id, String content, Instant sentAt, String platformMessageId) {
    return new Message(id, content, sentAt, platformMessageId);
  }
}
