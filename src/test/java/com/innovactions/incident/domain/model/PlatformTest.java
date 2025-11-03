package com.innovactions.incident.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlatformTest {

  @Test
  @DisplayName("should contain exactly WHATSAPP and SLACK")
  void shouldContainExpectedValues() {
    assertThat(Platform.values()).containsExactly(Platform.WHATSAPP, Platform.SLACK);
  }

  @Test
  @DisplayName("should resolve from string name correctly")
  void shouldResolveFromName() {
    assertThat(Platform.valueOf("WHATSAPP")).isEqualTo(Platform.WHATSAPP);
    assertThat(Platform.valueOf("SLACK")).isEqualTo(Platform.SLACK);
  }

  @Test
  @DisplayName("toString should match name() by default")
  void toStringShouldMatchName() {
    for (Platform platform : Platform.values()) {
      assertThat(platform.toString()).isEqualTo(platform.name());
    }
  }
}
