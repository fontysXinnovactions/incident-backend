package com.innovactions.incident.port.inbound;

import java.util.Map;

public interface WhatsAppMessageReceiverPort {
    void handle(Map<String, Object> payload);
}
