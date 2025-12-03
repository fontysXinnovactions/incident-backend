package com.innovactions.incident.adapter.outbound.AI;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.innovactions.incident.port.outbound.IncidentDetectorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiIncidentDetector implements IncidentDetectorPort {

  private final Client client;

  public GeminiIncidentDetector() {
    this.client = new Client(); // reads GEMINI_API_KEY from env
  }

  /**
   * Determines if a message describes an incident.
   *
   * @param message The text message to evaluate.
   * @return true if it's an incident, false otherwise.
   */
  public boolean isIncident(String message) {
    String prompt =
        """
                You are an incident triage assistant.
                Decide if the following messages describes a real INCIDENT or not.

                Definition:
                - INCIDENT: message reports a problem, outage, bug, failure, security issue, or something not working as expected.
                - NOT_INCIDENT: any normal conversation, greeting, question, update, or unrelated message.

                Return exactly one word: INCIDENT or NOT_INCIDENT.

                Messages:
                ---
                %s
                ---
                """
            .formatted(message == null ? "" : message);

    try {
      GenerateContentResponse response =
          client.models.generateContent("gemini-2.5-flash", prompt, null);
      String result = response.text();

      log.info("Gemini AI classification result: {}", result);

      if (result == null) return false;

      result = result.trim().toUpperCase();
      return result.equals("INCIDENT");

    } catch (Exception e) {
      log.error("Error calling Gemini AI for incident detection", e);
      return false; // fallback to non-incident if AI fails
    }
  }
}
