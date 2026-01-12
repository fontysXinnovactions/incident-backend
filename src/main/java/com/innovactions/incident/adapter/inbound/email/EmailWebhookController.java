package com.innovactions.incident.adapter.inbound.email;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook/email")
public class EmailWebhookController {

  private final EmailInboundAdapter adapter;

  public EmailWebhookController(EmailInboundAdapter adapter) {
    this.adapter = adapter;
  }

  // Validation via GET
  @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE, params = "validationToken")
  public ResponseEntity<String> validateGet(@RequestParam("validationToken") String token) {
    log.info("📬 Received GET validation token: {}", token);
    return ResponseEntity.ok(token);
  }

  // Validation via POST (fallback)
  @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE, params = "validationToken")
  public ResponseEntity<String> validatePost(@RequestParam("validationToken") String token) {
    log.info("📬 Received POST validation token: {}", token);
    return ResponseEntity.ok(token);
  }

  // real notifications
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> handleNotification(@RequestBody Map<String, Object> payload) {
    log.info("📨 Received notification payload: {}", payload);
    adapter.processNotification(payload);
    return ResponseEntity.ok().build();
  }
}
