package com.innovactions.incident.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class SeverityTest {

    @Test
    @DisplayName("each severity should have correct rank order")
    void shouldHaveCorrectRanks() {
        assertThat(Severity.MINOR.getRank()).isEqualTo(1);
        assertThat(Severity.MAJOR.getRank()).isEqualTo(2);
        assertThat(Severity.URGENT.getRank()).isEqualTo(3);
    }

    @Nested
    @DisplayName("next()")
    class NextTests {

        @Test
        @DisplayName("should return next severity when available")
        void shouldReturnNextSeverity() {
            assertThat(Severity.MINOR.next()).contains(Severity.MAJOR);
            assertThat(Severity.MAJOR.next()).contains(Severity.URGENT);
        }

        @Test
        @DisplayName("should return empty when already URGENT")
        void shouldReturnEmptyWhenUrgent() {
            Optional<Severity> next = Severity.URGENT.next();
            assertThat(next).isEmpty();
        }
    }

    @Nested
    @DisplayName("isHigherThan()")
    class IsHigherThanTests {

        @Test
        @DisplayName("should compare ranks correctly")
        void shouldCompareCorrectly() {
            assertThat(Severity.MAJOR.isHigherThan(Severity.MINOR)).isTrue();
            assertThat(Severity.URGENT.isHigherThan(Severity.MAJOR)).isTrue();
            assertThat(Severity.MINOR.isHigherThan(Severity.URGENT)).isFalse();
            assertThat(Severity.MINOR.isHigherThan(Severity.MINOR)).isFalse();
        }

        @Test
        @DisplayName("should handle all pairwise comparisons consistently")
        void shouldHandleAllComparisons() {
            for (Severity s1 : Severity.values()) {
                for (Severity s2 : Severity.values()) {
                    boolean expected = s1.getRank() > s2.getRank();
                    assertThat(s1.isHigherThan(s2))
                            .as("%s higher than %s", s1, s2)
                            .isEqualTo(expected);
                }
            }
        }
    }
}
