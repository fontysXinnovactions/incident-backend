package com.innovactions.incident.application;

import java.time.Instant;

public record InboundMessage(
    String channel, // e.g. "whatsapp"
    String from, // wa_id (phone number)
    String senderName, // contact profile name
    String text, // message body
    Instant timestamp // actual WhatsApp timestamp
    ) {}
