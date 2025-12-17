package com.innovactions.incident.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.innovactions.incident.adapter.outbound.persistence.MessagesJpaRepository;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.domain.model.*;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ConversationContextServiceTest {

  @Mock private IncidentPersistencePort persistence;

  @Mock private MessagesJpaRepository messagesJpaRepository;

  private ConversationContextService service;

  @BeforeEach
  void setUp() {
    persistence = mock(IncidentPersistencePort.class);
    messagesJpaRepository = mock(MessagesJpaRepository.class);
    service = new ConversationContextService(persistence, messagesJpaRepository);
  }

  @Nested
  @DisplayName("hasActiveContext()")
  class HasActiveContextTests {

    @Test
    @DisplayName("should return false when no active context found")
    void shouldReturnFalseWhenNoContext() {
      // Given
      var command =
          CreateIncidentCommand.builder()
              .reporterId("u1")
              .message("system down")
              .timestamp(Instant.now())
              .build();
      Status status = Status.OPEN;

      when(persistence.existsByReporter("u1", status)).thenReturn(false);

      // When
      var result = service.hasActiveContext(command);

      // Then
      assertThat(result).isFalse();
      verify(persistence).existsByReporter("u1", Status.OPEN);
    }
  }

  @Nested
  @DisplayName("saveNewIncident()")
  class SaveNewIncidentTests {
    @Test
    @DisplayName("should save new conversation with prefixed incident id")
    void shouldSaveNewIncident() {
      // Given
      Instant t = Instant.now();
      String channelId = "channel-123";
      Severity severity = Severity.MAJOR;
      var command =
          CreateIncidentCommand.builder()
              .reporterId("u1")
              .reporterName("user")
              .message("down time")
              .platform(Platform.WHATSAPP)
              .timestamp(t)
              .build();

      // When
      service.saveNewIncident(command, channelId, severity);

      // Then
      verify(persistence)
          .saveNewIncident(
              argThat(
                  incident ->
                      incident.getReporterId().equals("u1")
                          && incident.getReporterName().equals("user")
                          && incident.getDetails().equals("down time")
                          && incident.getSeverity() == Severity.MAJOR
                          && incident.getAssignee().equals("Developer")),
              eq(channelId));
    }
  }
}
