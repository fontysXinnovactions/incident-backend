package com.innovactions.incident.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.domain.model.ConversationContext;
import com.innovactions.incident.port.outbound.ConversationRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConversationContextServiceTest {

  private ConversationRepositoryPort repository;
  private ConversationContextService service;

  @BeforeEach
  void setUp() {
    repository = mock(ConversationRepositoryPort.class);
    service = new ConversationContextService(repository);
  }

  @Nested
  @DisplayName("hasActiveContext()")
  class HasActiveContextTests {

    @Test
    @DisplayName("should return false when no active context found")
    void shouldReturnFalseWhenNoContext() {
      // Given
      var command =
          CreateIncidentCommand.builder().reporterId("user-1").timestamp(Instant.now()).build();

      when(repository.findActiveByUser("user-1")).thenReturn(Optional.empty());

      // When
      boolean result = service.hasActiveContext(command);

      // Then
      assertThat(result).isFalse();
      verify(repository).findActiveByUser("user-1");
    }

    @Test
    @DisplayName("should return true when context exists and is not expired")
    void shouldReturnTrueWhenActive() {
      // Given
      Instant now = Instant.now();
      var command = CreateIncidentCommand.builder().reporterId("user-1").timestamp(now).build();

      var ctx =
          new ConversationContext(
              "user-1", "incident-1", "channel-1", now.minus(Duration.ofHours(1)), true);

      when(repository.findActiveByUser("user-1")).thenReturn(Optional.of(ctx));

      // When
      boolean result = service.hasActiveContext(command);

      // Then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should return false when context is older than 24 hours")
    void shouldReturnFalseWhenExpired() {
      // Given
      Instant now = Instant.now();
      var command = CreateIncidentCommand.builder().reporterId("user-1").timestamp(now).build();

      var ctx =
          new ConversationContext(
              "user-1",
              "incident-1",
              "channel-1",
              now.minus(Duration.ofHours(25)), // expired
              true);

      when(repository.findActiveByUser("user-1")).thenReturn(Optional.of(ctx));

      // When
      boolean result = service.hasActiveContext(command);

      // Then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("findValidUpdateContext()")
  class FindValidUpdateContextTests {

    @Test
    @DisplayName("should return null when no active context found")
    void shouldReturnNullWhenNoContext() {
      // Given
      var command =
          CreateIncidentCommand.builder()
              .reporterId("u1")
              .message("something")
              .timestamp(Instant.now())
              .build();

      when(repository.findActiveByUser("u1")).thenReturn(Optional.empty());

      // When
      var result = service.findValidUpdateContext(command);

      // Then
      assertThat(result).isNull();
      verify(repository).findActiveByUser("u1");
    }

    @Test
    @DisplayName("should return null when context exists but expired")
    void shouldReturnNullWhenExpired() {
      // Given
      Instant now = Instant.now();
      var command =
          CreateIncidentCommand.builder()
              .reporterId("u1")
              .message("expired")
              .timestamp(now)
              .build();

      var oldCtx = new ConversationContext("u1", "i1", "c1", now.minus(Duration.ofHours(30)), true);

      when(repository.findActiveByUser("u1")).thenReturn(Optional.of(oldCtx));

      var spyService = spy(service);
      doReturn(false).when(spyService).hasActiveContext(command);

      // When
      var result = spyService.findValidUpdateContext(command);

      // Then
      assertThat(result).isNull();
      verify(repository).findActiveByUser("u1");
    }

    @Test
    @DisplayName(
        "should update repository and return UpdateIncidentCommand when active context exists")
    void shouldUpdateAndReturnCommand() {
      // Given
      Instant now = Instant.now();
      var command =
          CreateIncidentCommand.builder()
              .reporterId("u1")
              .message("new info")
              .timestamp(now)
              .build();

      var ctx = new ConversationContext("u1", "i1", "c1", now.minus(Duration.ofHours(1)), true);

      when(repository.findActiveByUser("u1")).thenReturn(Optional.of(ctx));

      // Force hasActiveContext() to true
      var spyService = spy(service);
      doReturn(true).when(spyService).hasActiveContext(command);

      // When
      var result = spyService.findValidUpdateContext(command);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.channelId()).isEqualTo("c1");
      assertThat(result.message()).isEqualTo("new info");
      assertThat(result.updatedAt()).isEqualTo(now);

      verify(repository).update("u1", "i1", "c1", now);
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
      var command = CreateIncidentCommand.builder().reporterId("u1").timestamp(t).build();

      // When
      service.saveNewIncident(command, "C-001");

      // Then
      verify(repository).saveNew("u1", "INCIDENT-" + t, "C-001", t);
    }
  }
}
