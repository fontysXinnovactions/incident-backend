package com.innovactions.incident.adapter.inbound.whatsapp;

import com.innovactions.incident.adapter.inbound.whatsapp.mapper.WhatsAppPayloadToCreateIncidentCommandMapper;
import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {
    private final IncidentInboundPort incidentInboundPort;
    @Value("${whatsapp.verifyToken}")
    private String verifyToken;

    //TODO: more error handling
    //TODO: Give more explanation
    //TODO: make params required and add validation of some sort

    /**
     * Use case: webhook verification
     * checks Meta developer dashboard for the webhook URL and verify token
     * establishes connection with WhatsApp Cloud API
     * Once the webhook verification succeeds returns challenge
     * URL will be reachable
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully");
            log.info(challenge);
            return ResponseEntity.ok(challenge);  // Respond with challenge
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody WhatsAppPayload payload) {
        if (payload == null || payload.getEntry() == null || payload.getEntry().isEmpty()) {
            log.warn("Received empty WhatsApp webhook payload");
            return ResponseEntity.badRequest().build();
        }

        try {
            log.debug("Incoming WhatsApp webhook received with {} entries", payload.getEntry().size());

            CreateIncidentCommand message = WhatsAppPayloadToCreateIncidentCommandMapper.map(payload);
            incidentInboundPort.handlePossibleIncident(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process WhatsApp webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
