package com.innovactions.incident.adapter.outbound;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.port.outbound.IncidentSeverityClassifierPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class GeminiIncidentClassifier implements IncidentSeverityClassifierPort {

    private final Models models;

    public GeminiIncidentClassifier() {
        Client client = new Client();
        this.models = client.models;
    }

    // For testing
    public GeminiIncidentClassifier(Models models) {
        this.models = models;
    }

    @Override
    public Severity classifyIncident(String message) {
        String prompt = """
                 You are an incident triage assistant.
                 Classify ONLY the severity for the user's report.
                 
                 Options (return EXACTLY one token):
                 - URGENT: immediate attention, show-stopper, downtime, security breach, production outage.
                 - MAJOR: broken functionality but workarounds exist; handle within this week.
                 - MINOR: small bug/annoyance; can be deferred.
                 
                 Return just one of: URGENT, MAJOR, MINOR.
                 If no report is provided, return.
                 
                 Report:
                 ---
                 %s
                 ---
                """.formatted(message == null ? "" : message);

        GenerateContentResponse response = models.generateContent(
                "gemini-2.5-flash",
                prompt,
                null
        );

        log.info("Gemini AI classified incident message '{}' as '{}'", message, response);

        try {
            return Severity.valueOf(Objects.requireNonNull(response.text()).trim().toUpperCase());
        } catch (NullPointerException e) {
            log.warn("Could not map response '{}', falling back to MINOR", response);
            return Severity.MINOR;
        }
    }
}
