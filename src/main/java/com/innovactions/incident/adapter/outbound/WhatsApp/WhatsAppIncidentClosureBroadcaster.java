package com.innovactions.incident.adapter.outbound.WhatsApp;

import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppIncidentClosureBroadcaster implements IncidentReporterNotifierPort {
  private final WhatsAppOutboundAdapter whatsAppOutboundAdapter;

  @Override
  public void notifyReporter(String reporterId, String reason) {
    try {
      String message = "✅ Your reported incident has been closed.\nReason: " + reason;

      // In WhatsApp's case, channelId = reporter's phone number
      whatsAppOutboundAdapter.sendTextMessage(reporterId, message);

      log.info("Notified WhatsApp reporter {} about incident closure", reporterId);
    } catch (Exception e) {
      log.error("Error notifying WhatsApp reporter {}: {}", reporterId, e.getMessage(), e);
    }
  }

  @Override
  public String getPlatformName() {
    return "whatsapp";
  }
}
