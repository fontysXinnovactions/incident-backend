package com.innovactions.incident.port.outbound;

public interface WhatsAppOutboundPort {
  void sendTextMessage(String to, String message);
}
