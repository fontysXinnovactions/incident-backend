package com.innovactions.incident.adapter.outbound.WhatsApp;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class WhatsAppTestController {
  private final WhatsAppOutboundAdapter outboundAdapter;

  @GetMapping("/send")
  public String sendTestMessage() {
    outboundAdapter.sendTextMessage("31619315253", "Hello from Outbound POC");
    return "Message triggered!";
  }
}
