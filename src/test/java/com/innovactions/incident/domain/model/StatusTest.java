package com.innovactions.incident.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTest {

  @Test
  @DisplayName("should contain all defined statuses in order")
  void shouldContainExpectedValues() {
    assertThat(Status.values())
        .containsExactly(Status.OPEN, Status.ASSIGNED, Status.RESOLVED, Status.DISMISSED);
  }

  @Test
  @DisplayName("should return enum by name correctly")
  void shouldResolveFromName() {
    assertThat(Status.valueOf("OPEN")).isEqualTo(Status.OPEN);
    assertThat(Status.valueOf("RESOLVED")).isEqualTo(Status.RESOLVED);
  }

  @Test
  @DisplayName("toString should return the same as name()")
  void toStringShouldMatchName() {
    for (Status status : Status.values()) {
      assertThat(status.toString()).isEqualTo(status.name());
    }
  }
}
