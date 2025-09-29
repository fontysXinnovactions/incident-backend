package com.innovactions.incident.controller;

import com.innovactions.incident.port.inbound.WhatsAppMessageReceiverPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {
    private final WhatsAppMessageReceiverPort messageReceiverPort;
    @Value("${whatsapp.verifyToken}")
    private String verifyToken;
    // GET endpoint for webhook verification
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            System.out.println("WEBHOOK VERIFIED");
            return ResponseEntity.ok(challenge);  // Respond with challenge
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }
    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            log.warn("Received empty WhatsApp webhook payload");
            return ResponseEntity.badRequest().build();
        }

        log.debug("Incoming WhatsApp webhook payload: {}", body);
        messageReceiverPort.handle(body);
        return ResponseEntity.ok().build();
    }
}
