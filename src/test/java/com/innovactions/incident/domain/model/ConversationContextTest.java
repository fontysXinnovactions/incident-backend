package com.innovactions.incident.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class ConversationContextTest {

    @Test
    @DisplayName("should store and expose all constructor parameters correctly")
    void shouldStoreConstructorParameters() {
        Instant now = Instant.now();

        var ctx = new ConversationContext(
                "whatsapp:+31612345678",
                "incident-123",
                "C12345",
                now,
                true
        );

        assertThat(ctx.userId()).isEqualTo("whatsapp:+31612345678");
        assertThat(ctx.incidentId()).isEqualTo("incident-123");
        assertThat(ctx.channelId()).isEqualTo("C12345");
        assertThat(ctx.lastMessageAt()).isEqualTo(now);
        assertThat(ctx.active()).isTrue();
    }

    @Test
    @DisplayName("equals and hashCode should depend on all record components")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Instant t = Instant.now();

        var ctx1 = new ConversationContext("u1", "i1", "c1", t, true);
        var ctx2 = new ConversationContext("u1", "i1", "c1", t, true);
        var ctx3 = new ConversationContext("u1", "i1", "c2", t, true);

        assertThat(ctx1).isEqualTo(ctx2).hasSameHashCodeAs(ctx2);
        assertThat(ctx1).isNotEqualTo(ctx3);
    }

    @Test
    @DisplayName("toString should include all field values")
    void shouldIncludeAllValuesInToString() {
        var ctx = new ConversationContext("uX", "iY", "cZ", Instant.parse("2025-11-03T12:00:00Z"), false);
        String str = ctx.toString();

        assertThat(str)
                .contains("uX")
                .contains("iY")
                .contains("cZ")
                .contains("2025-11-03T12:00:00Z")
                .contains("false");
    }
}
