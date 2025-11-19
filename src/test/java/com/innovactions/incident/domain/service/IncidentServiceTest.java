package com.innovactions.incident.domain.service;

import static org.assertj.core.api.Assertions.*;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.application.command.UpdateIncidentCommand;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Platform;
import com.innovactions.incident.domain.model.Severity;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IncidentServiceTest {

  private IncidentService service;

  @BeforeEach
  void setUp() {
    service = new IncidentService();
  }

  @Nested
  @DisplayName("createIncident()")
  class CreateIncidentTests {

    @Test
    @DisplayName("should create a valid Incident with correct values")
    void shouldCreateIncident() {
      // Given
      var cmd =
          CreateIncidentCommand.builder()
              .reporterId("rep-123")
              .reporterName("Alice")
              .message("Database outage")
              .timestamp(Instant.now())
              .platform(Platform.SLACK)
              .build();

      // When
      Incident result = service.createIncident(cmd, Severity.MAJOR);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getReporterId()).isEqualTo("rep-123");
      assertThat(result.getReporterName()).isEqualTo("Alice");
      assertThat(result.getDetails()).isEqualTo("Database outage");
      assertThat(result.getSeverity()).isEqualTo(Severity.MAJOR);
      assertThat(result.getAssignee()).isEqualTo("Bob");
      assertThat(result.getId()).isNotNull(); // UUID auto-generated
    }

    @Test
    @DisplayName("Should throw when given null command")
    void shouldThrowWhenCommandNull() {
      assertThatThrownBy(() -> service.createIncident(null, Severity.MINOR))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should throw when given null severity")
    void shouldThrowWhenSeverityNull() {
      // Given
      var cmd =
          CreateIncidentCommand.builder()
              .reporterId("id")
              .reporterName("John")
              .message("Something broke")
              .timestamp(Instant.now())
              .platform(Platform.WHATSAPP)
              .build();

      // When, Then
      assertThatThrownBy(() -> service.createIncident(cmd, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should allow blank message without throwing")
    void shouldAllowBlankMessage() {
      // Given
      var cmd =
          CreateIncidentCommand.builder()
              .reporterId("r1")
              .reporterName("Bob")
              .message(" ")
              .timestamp(Instant.now())
              .platform(Platform.WHATSAPP)
              .build();

      // When
      Incident result = service.createIncident(cmd, Severity.MINOR);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getDetails()).isEqualTo(" ");
    }
  }

  @Nested
  @DisplayName("updateIncident()")
  class UpdateIncidentTests {

    @Test
    @DisplayName("should create an updated Incident with a generated UUID id")
    void shouldUpdateIncident() {
      // Given
      var updateCmd =
          UpdateIncidentCommand.builder()
              .channelId("INC-42")
              .message("Updated details")
              .updatedAt(Instant.now())
              .build();

      var createCmd =
          CreateIncidentCommand.builder()
              .reporterId("rep-123")
              .reporterName("Alice")
              .message("Original message")
              .timestamp(Instant.now())
              .platform(Platform.SLACK)
              .build();

      // When
      Incident updated = service.updateIncident(updateCmd, createCmd);

      // Then
      assertThat(updated).isNotNull();
      assertThat(updated.getId()).isInstanceOf(java.util.UUID.class);
      assertThat(updated.getReporterId()).isEqualTo("rep-123");
      assertThat(updated.getReporterName()).isEqualTo("Alice");
      assertThat(updated.getSeverity()).isEqualTo(Severity.MINOR);
      assertThat(updated.getAssignee()).isEqualTo("Bob");
      assertThat(updated.getDetails()).isEqualTo("Updated details");
    }

    @Test
    @DisplayName("should throw NullPointerException when update command is null")
    void shouldThrowWhenUpdateCommandNull() {
      // Given
      var createCmd =
          CreateIncidentCommand.builder()
              .reporterId("rep-123")
              .reporterName("Alice")
              .message("Original message")
              .timestamp(Instant.now())
              .platform(Platform.SLACK)
              .build();

      // When, Then
      assertThatThrownBy(() -> service.updateIncident(null, createCmd))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when message is null")
    void shouldThrowWhenNullFields() {
      // Given
      var updateCmd =
          UpdateIncidentCommand.builder().channelId("INC-99").message(null).updatedAt(null).build();

      var createCmd =
          CreateIncidentCommand.builder()
              .reporterId("rep-123")
              .reporterName("Alice")
              .message("Original message")
              .timestamp(Instant.now())
              .platform(Platform.SLACK)
              .build();

      // When, Then
      assertThatThrownBy(() -> service.updateIncident(updateCmd, createCmd))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when create command is null")
    void shouldThrowWhenCreateCommandNull() {
      // Given
      var updateCmd =
          UpdateIncidentCommand.builder()
              .channelId("INC-99")
              .message("Updated message")
              .updatedAt(Instant.now())
              .build();

      // When, Then
      assertThatThrownBy(() -> service.updateIncident(updateCmd, null))
          .isInstanceOf(NullPointerException.class);
    }
  }
}
