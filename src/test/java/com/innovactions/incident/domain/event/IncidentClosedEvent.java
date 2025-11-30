package com.innovactions.incident.domain.event;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IncidentClosedEventTest {

  @Test
  @DisplayName("should store all constructor parameters correctly")
  void shouldStoreConstructorParameters() {
    var event =
        new IncidentClosedEvent("slack-ch01", "user-123", "slack", "Issue resolved by operator");

    assertThat(event.reporterId()).isEqualTo("user-123");
    assertThat(event.platform()).isEqualTo("slack");
    assertThat(event.reason()).isEqualTo("Issue resolved by operator");
  }

  @Test
  @DisplayName("should implement equals and hashCode based on all fields")
  void shouldImplementEqualsAndHashCode() {
    var e1 = new IncidentClosedEvent("slack-ch01", "r1", "whatsapp", "done");
    var e2 = new IncidentClosedEvent("slack-ch01", "r1", "whatsapp", "done");
    var e3 = new IncidentClosedEvent("slack-ch03", "r2", "slack", "done");

    assertThat(e1).isEqualTo(e2).hasSameHashCodeAs(e2);
    assertThat(e1).isNotEqualTo(e3);
  }

  @Test
  @DisplayName("toString should include reporterId, platform, and reason")
  void shouldIncludeAllValuesInToString() {
    var e = new IncidentClosedEvent("slack-ch01", "rep-1", "email", "duplicate");
    String str = e.toString();

    assertThat(str).contains("rep-1").contains("email").contains("duplicate");
  }
}
