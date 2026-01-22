package com.innovactions.incident.domain.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.innovactions.incident.domain.model.Severity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ChannelNameGeneratorTest {

  /** Happy route test, whether function creates proper string. */
  @Test
  void generateChannelName_shouldFormatLowercaseSeverityAndTimestamp() {
    // Given
    LocalDateTime fixed = LocalDateTime.of(2025, 11, 3, 7, 5);

    try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
      mocked.when(LocalDateTime::now).thenReturn(fixed);

      ChannelNameGenerator generator = new ChannelNameGenerator();

      // When
      String channel = generator.generateChannelName(Severity.MAJOR, "test");

      // Then
      assertEquals("major_test_03-11-2025_07-05-00", channel);
    }
  }

  @Test
  void shouldZeroPadSingleDigits() {
    // Given
    LocalDateTime fixed = LocalDateTime.of(2025, 1, 2, 3, 4);

    // When
    try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class)) {
      mocked.when(() -> LocalDateTime.now()).thenReturn(fixed);

      ChannelNameGenerator gen = new ChannelNameGenerator();
      String result = gen.generateChannelName(Severity.MINOR, "test");

      // Then
      assertEquals("minor_test_02-01-2025_03-04-00", result);
    }
  }

  @Test
  void shouldFailOnNullSeverity() {
    // Given
    var gen = new ChannelNameGenerator();

    // When, Then
    assertThatThrownBy(() -> gen.generateChannelName(null, "test"))
        .isInstanceOf(NullPointerException.class) // or IllegalArgumentException if you add a guard
        .hasMessageContaining("severity"); // add a message in your guard for clarity
  }
}
