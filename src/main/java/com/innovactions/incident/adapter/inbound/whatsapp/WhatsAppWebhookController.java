package com.innovactions.incident.adapter.inbound.whatsapp;

import com.innovactions.incident.adapter.inbound.whatsapp.mapper.WhatsAppIncidentCommandMapper;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import java.util.List;
import java.util.stream.Collectors;

import com.innovactions.incident.port.outbound.IncidentDetectorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles incoming webhooks from the WhatsApp Cloud API.
 *
 * <p>This controller serves two purposes:<br>
 * - Webhook verification (GET) — allows Meta to verify the endpoint using a token challenge.<br>
 * - Message processing (POST) — receives user messages and forwards them into the incident
 * workflow.
 */
@Slf4j
@RestController
@RequestMapping("/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

  private final IncidentInboundPort incidentInboundPort;
  private final IncidentDetectorPort incidentDetectorPort;

  /** The 'verify' token from the Meta Developer dashboard. */
  @Value("${whatsapp.verifyToken}")
  private String verifyToken;

  /**
   * Webhook verification endpoint, required by WhatsApp Cloud API during initial setup.
   *
   * <p>When Meta verifies your webhook URL, it sends a GET request with query parameters: <code>
   * hub.mode</code>, <code>hub.verify_token</code>, and <code>hub.challenge</code>.
   *
   * <p>If the mode is <code>subscribe</code> and the 'verify' token matches, you must respond with
   * the challenge string.
   */
  @GetMapping
  public ResponseEntity<String> verifyWebhook(
      @RequestParam(value = "hub.mode", required = false) String mode,
      @RequestParam(value = "hub.verify_token", required = false) String token,
      @RequestParam(value = "hub.challenge", required = false) String challenge) {

    boolean isValid = "subscribe".equals(mode) && verifyToken.equals(token);
    if (isValid) {
      log.info("WhatsApp webhook verified successfully");
      return ResponseEntity.ok(challenge); // Return challenge as plain text
    }

    log.warn("WhatsApp webhook verification failed: mode={}, token={}", mode, token);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
  }

  /** Receives webhook events from WhatsApp Cloud API. */
  @PostMapping
  public ResponseEntity<Void> receiveWebhook(
      @RequestBody(required = false) WhatsAppPayload payload) {
    if (payload == null || payload.getEntry() == null || payload.getEntry().isEmpty()) {
      log.warn("Received empty or invalid WhatsApp webhook payload");
      return ResponseEntity.badRequest().build();
    }

    try {
      log.debug("Incoming WhatsApp webhook received with {} entries", payload.getEntry().size());

      List<WhatsAppPayload.Message> messages = List.of();

      try {
        messages = payload.getEntry().getFirst().getChanges().getFirst().getValue().getMessages();

      } catch (Exception ignored) {}

        String messageText = messages.stream()
                .map(m -> m.getText() != null ? m.getText().getBody() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        boolean isIncident = incidentDetectorPort.isIncident(messageText);

      if (isIncident) {
        CreateIncidentCommand command = WhatsAppIncidentCommandMapper.map(payload);
        incidentInboundPort.reportIncident(command);

        log.info(
            "Successfully processed WhatsApp message from incident reporter {}",
            command.reporterId());
      } else {
        log.info("Message is not an incident. No incident created.");
      }

      return ResponseEntity.ok().build();

    } catch (Exception e) {
      log.error("Failed to process WhatsApp webhook payload", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
