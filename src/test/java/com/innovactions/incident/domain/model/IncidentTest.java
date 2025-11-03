package com.innovactions.incident.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IncidentTest {

  private Incident incident;

  @BeforeEach
  void setUp() {
    incident = new Incident("rep-1", "Alice", "Database down", Severity.MINOR, "Bob");
  }

  @Test
  @DisplayName("should initialize with generated id and default status OPEN")
  void shouldInitializeCorrectly() {
    assertThat(incident.getId()).isNotNull().isInstanceOf(UUID.class);
    assertThat(incident.getStatus()).isEqualTo(Status.OPEN);
    assertThat(incident.getReportedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(incident.getSeverity()).isEqualTo(Severity.MINOR);
    assertThat(incident.getAssignee()).isEqualTo("Bob");
    assertThat(incident.getDetails()).isEqualTo("Database down");
  }

  @Nested
  @DisplayName("escalate()")
  class EscalateTests {

    @Test
    @DisplayName("should escalate severity from MINOR → MAJOR → URGENT")
    void shouldEscalate() {
      boolean first = incident.escalate();
      assertThat(first).isTrue();
      assertThat(incident.getSeverity()).isEqualTo(Severity.MAJOR);

      boolean second = incident.escalate();
      assertThat(second).isTrue();
      assertThat(incident.getSeverity()).isEqualTo(Severity.URGENT);
    }

    @Test
    @DisplayName("should not escalate beyond URGENT")
    void shouldNotEscalateBeyondUrgent() {
      incident = new Incident("r", "n", "x", Severity.URGENT, "a");
      boolean result = incident.escalate();
      assertThat(result).isFalse();
      assertThat(incident.getSeverity()).isEqualTo(Severity.URGENT);
    }
  }

  @Nested
  @DisplayName("resolve()")
  class ResolveTests {
    @Test
    void shouldSetStatusResolved() {
      incident.resolve();
      assertThat(incident.getStatus()).isEqualTo(Status.RESOLVED);
    }
  }

  @Nested
  @DisplayName("reassign()")
  class ReassignTests {
    @Test
    void shouldChangeAssignee() {
      incident.reassign("Charlie");
      assertThat(incident.getAssignee()).isEqualTo("Charlie");
    }
  }

  @Nested
  @DisplayName("updateDetails()")
  class UpdateDetailsTests {
    @Test
    void shouldUpdateDetails() {
      incident.updateDetails("API timeout observed");
      assertThat(incident.getDetails()).isEqualTo("API timeout observed");
    }
  }

  @Nested
  @DisplayName("summary()")
  class SummaryTests {
    @Test
    void shouldReturnReadableSummary() {
      String summary = incident.summary();

      assertThat(summary)
          .contains("📢 Incident")
          .contains("Assigned to Bob")
          .contains("Reporter: Alice")
          .contains("Status: OPEN")
          .contains("Details: Database down")
          .contains("MINOR");
    }
  }
}
