package com.innovactions.incident.adapter.outbound.WhatsApp;

import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppIncidentReporterNotifierAdapter implements IncidentReporterNotifierPort {
    private final WhatsAppOutboundAdapter whatsAppOutboundAdapter;
    /**
     * Notifying incident reporters through WhatsApp
     * when an incident has been closed.
     * <p>
     * Implements the {@link IncidentReporterNotifierPort} to send closure messages
     * to the reporter using the {@link WhatsAppOutboundAdapter}.
     * <p>
     * In this context, the reporter's ID corresponds to the WhatsApp phone number,
     * which is used as the destination for the notification message.
     */

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
