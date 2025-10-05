package com.innovactions.incident.port.inbound;

import com.innovactions.incident.application.InboundMessage;

public interface WhatsAppMessageReceiverPort {
    void handle(InboundMessage message);
}
