package com.innovactions.incident.adapter.outbound;

import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.port.outbound.IncidentSeverityClassifierPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GeminiIncidentClassifier}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeminiIncidentClassifierTest {

    @Mock
    private Models models;

    @Mock
    private GenerateContentResponse response;

    private IncidentSeverityClassifierPort classifier;

    @BeforeEach
    void setUp() {
        classifier = new GeminiIncidentClassifier(models);
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * This test verifies whether the function correctly maps a returned {@code URGENT}
     * token to {@link Severity#URGENT}.
     */
    @Test
    void shouldClassifyUrgent() {
        // Given
        when(models.generateContent(eq("gemini-2.5-flash"), anyString(), isNull()))
                .thenReturn(response);
        when(response.text()).thenReturn("URGENT");

        // When
        Severity result = classifier.classifyIncident("Production outage, customers cannot log in.");

        // Then
        assertThat(result).isEqualTo(Severity.URGENT);
        verify(models).generateContent(eq("gemini-2.5-flash"), anyString(), isNull());
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * Verifies mapping is case-insensitive and trims whitespace in the model's text response.
     */
    @Test
    void shouldTrimAndUppercaseResponse() {
        // Given
        when(models.generateContent(eq("gemini-2.5-flash"), anyString(), isNull()))
                .thenReturn(response);
        when(response.text()).thenReturn("   major   ");

        // When
        Severity result = classifier.classifyIncident("Feature X broken, workaround exists.");

        // Then
        assertThat(result).isEqualTo(Severity.MAJOR);
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * Verifies that when the model returns {@code null} text, we fall back to {@link Severity#MINOR}.
     * This triggers the {@link NullPointerException} handling path guarded in the code.
     */
    @Test
    void shouldFallbackToMinorWhenResponseTextIsNull() {
        // Given
        when(models.generateContent(eq("gemini-2.5-flash"), anyString(), isNull()))
                .thenReturn(response);
        when(response.text()).thenReturn(null);

        // When
        Severity result = classifier.classifyIncident("Small typo in UI label.");

        // Then
        assertThat(result).isEqualTo(Severity.MINOR);
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * Verifies we safely handle a {@code null} incoming message (prompt gets an empty report body)
     * and still classify correctly when the model returns a valid token.
     *
     * TODO: should we even accept empty messages?
     */
    @Test
    void shouldHandleNullMessageSafely() {
        // Given
        when(models.generateContent(
                eq("gemini-2.5-flash"),
                (String) argThat((String prompt) ->
                        prompt != null &&
                                prompt.contains("Report:") &&
                                prompt.contains("---")
                ),
                isNull()
        )).thenReturn(response);
        when(response.text()).thenReturn("minor");

        // When
        Severity result = classifier.classifyIncident(null);

        // Then
        assertThat(result).isEqualTo(Severity.MINOR);
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * Tests the behavior when Gemini returns an unexpected token.
     * <p>
     * <b>NOTE:</b> The current implementation does <i>not</i> catch {@link IllegalArgumentException}
     * thrown by {@link Severity#valueOf(String)} for invalid tokens. If we later decide to guard
     * against wrong tokens and fall back to {@link Severity#MINOR}, we need to update the production code and
     * change this test accordingly to assert {@code MINOR}.
     */
    @Test
    void shouldPropagateWhenResponseIsUnexpectedToken() {
        // Given
        when(models.generateContent(eq("gemini-2.5-flash"), anyString(), isNull()))
                .thenReturn(response);
        when(response.text()).thenReturn("SEVERE"); // not a valid enum constant

        // Then
        assertThatThrownBy(() -> classifier.classifyIncident("Weird token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Unit test for the {@link GeminiIncidentClassifier#classifyIncident}.
     *
     * Sanity check that we pass the original message into the prompt sent to the model.
     * We avoid asserting the entire prompt, and instead capture and assert
     * that the prompt contains the message and the static delimiters.
     */
    @Test
    void shouldIncludeMessageInPrompt() {
        // Given
        String message = "Payments degraded for EU customers.";
        when(models.generateContent(eq("gemini-2.5-flash"), anyString(), isNull()))
                .thenReturn(response);
        when(response.text()).thenReturn("URGENT");

        // When
        classifier.classifyIncident(message);

        // Then (capture prompt)
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(models).generateContent(eq("gemini-2.5-flash"), promptCaptor.capture(), isNull());
        String prompt = promptCaptor.getValue();
        assertThat(prompt)
                .contains("Report:")
                .contains("---")
                .contains(message);
    }
}
