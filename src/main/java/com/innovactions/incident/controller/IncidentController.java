package com.innovactions.incident.controller;

import java.util.List;

import com.innovactions.incident.adapter.outbound.WhatsApp.WhatsAppOutboundAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incident")
@RequiredArgsConstructor
public class IncidentController {
  private final WhatsAppOutboundAdapter whatsAppOutboundAdapter;
  @GetMapping
  public List<String> getAllIncidents() {
    return List.of("something");
  }

  @GetMapping("/{id}")
  public String getIncident(@PathVariable String id) {
    return "Incident with id: " + id;
  }

  @PostMapping
  public String createIncident(@RequestBody String description) {
    return "Incident Created";
  }

  @GetMapping("/reply")
  public String replyToMessage(){
    whatsAppOutboundAdapter.sendQuotedTextMessageHardcoded(
            "31619315253", // full phone number, no +
            "✅ Test reply: Issue is resolved. System is working again."
    );
    return "WhatsApp quoted reply sent (check your phone)";
  }
}
