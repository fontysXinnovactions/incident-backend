package com.innovactions.incident.adapter.outbound.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.innovactions.incident.domain.model.IncidentClassification;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GeminiIncidentClassifier implements SeverityClassifierPort {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final Client client;

  public GeminiIncidentClassifier(String apiKey) {
    this.client = Client.builder().apiKey(apiKey).build();
  }

  @Override
  public IncidentClassification classify(String message) {
    String prompt =
        """
                You are an incident triage assistant.

                Analyze the incident report and respond ONLY in valid JSON
                using the following schema:

                {
                  "severity": "URGENT | MAJOR | MINOR",
                  "summary": "One short sentence summary (max 20 words)"
                }

                Severity rules:
                - URGENT: immediate attention, downtime, security breach, production outage
                - MAJOR: broken functionality with workarounds
                - MINOR: small bug or annoyance

                Report:
                ---
                %s
                ---
                """
            .formatted(message == null ? "" : message);

    try {
      GenerateContentResponse response =
          client.models.generateContent("gemini-2.5-flash", prompt, null);

      String text = response.text();
      log.info("Gemini response: {}", text);

      JsonNode root = MAPPER.readTree(text);

      Severity severity = Severity.valueOf(root.get("severity").asText().trim().toUpperCase());

      String summary = root.get("summary").asText().trim();

      return new IncidentClassification(severity, summary);

    } catch (Exception e) {
      log.error("Gemini classify failed ({}). Falling back to MINOR.", e.getMessage(), e);
      return new IncidentClassification(
          Severity.MINOR, "Unable to summarize incident automatically");
    }
  }
}
