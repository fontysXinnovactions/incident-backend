package com.innovactions.incident.adapter.outbound.AI;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.innovactions.incident.domain.model.Severity;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GeminiIncidentClassifier implements SeverityClassifierPort {

  private final Client client;

  public GeminiIncidentClassifier(String apiKey) {
    this.client = Client.builder().apiKey(apiKey).build();
  }

  @Override
  public Severity classify(String message) {
    String prompt =
        """
                 You are an incident triage assistant.
                 Classify ONLY the severity for the user's report.
                \s
                 Options (return EXACTLY one token):
                 - URGENT: immediate attention, show-stopper, downtime, security breach, production outage.
                 - MAJOR: broken functionality but workarounds exist; handle within this week.
                 - MINOR: small bug/annoyance; can be deferred.
                \s
                 Return just one of: URGENT, MAJOR, MINOR.
                \s
                 Report:
                 ---
                 %s
                 ---
                """
            .formatted(message == null ? "" : message);

    try {
      GenerateContentResponse response =
          client.models.generateContent("gemini-2.5-flash", prompt, null);

      log.info("Gemini AI classified incident message '{}' as '{}'", message, response);

      try {
        return Severity.valueOf(Objects.requireNonNull(response.text()).trim().toUpperCase());
      } catch (NullPointerException | IllegalArgumentException e) {
        log.warn("Could not map response '{}', falling back to MINOR", response);
        return Severity.MINOR;
      }
    } catch (Exception e) {
      log.error("Gemini classify failed ({}). Falling back to MINOR.", e.getMessage(), e);
      return Severity.MINOR;
    }
  }
}
