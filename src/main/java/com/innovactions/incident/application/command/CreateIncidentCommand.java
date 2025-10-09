package com.innovactions.incident.application.command;

import com.innovactions.incident.application.Platform;
import java.time.Instant;
import lombok.Builder;

@Builder
public record CreateIncidentCommand(
    // Reporter id specifically refers to a platform
    // specific identification of the user reporting the
    // incident. Example: user id on slack, phone number on whatsapp
    String reporterId, String reporterName, String message, Instant timestamp, Platform platform) {}
