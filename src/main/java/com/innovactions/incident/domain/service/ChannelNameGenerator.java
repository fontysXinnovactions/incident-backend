package com.innovactions.incident.domain.service;

import com.innovactions.incident.domain.model.Severity;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelNameGenerator {

  public String generateChannelName(Severity severity, String reporterName) {
    LocalDateTime now = LocalDateTime.now();

    String day = String.format("%02d", now.getDayOfMonth());
    String month = String.format("%02d", now.getMonthValue());
    String year = String.valueOf(now.getYear());
    String hour = String.format("%02d", now.getHour());
    String minute = String.format("%02d", now.getMinute());
    String second = String.format("%02d", now.getSecond());

    String sanitizedName = sanitizeForChannelName(reporterName);
    log.info("Sanitized name: {}", sanitizedName);

    return String.format(
        "%s_%s_%s-%s-%s_%s-%s-%s",
        severity.name().toLowerCase(), sanitizedName, day, month, year, hour, minute, second);
  }

  private String sanitizeForChannelName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return "unknown";
    }
    // Slack channel names: lowercase, max 80 chars, alphanumeric + hyphens/underscores
    String sanitized =
        name.toLowerCase()
            .replaceAll("[^a-z0-9-_]", "_")
            .replaceAll("_{2,}", "_")
            .replaceAll("^_|_$", "");

    return sanitized.substring(0, Math.min(30, sanitized.length()));
  }
}
