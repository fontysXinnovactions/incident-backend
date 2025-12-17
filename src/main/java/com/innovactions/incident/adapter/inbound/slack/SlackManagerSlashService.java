package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.adapter.outbound.persistence.Entity.IncidentEntity;
import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.domain.model.Status;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.innovactions.incident.port.outbound.IncidentPersistencePort;
import com.innovactions.incident.port.outbound.UserInfoPort;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Use-case service for manager Slack slash commands (/view, /assign).
 *
 * <p>Keeps Slack-specific logic (authorization, parsing, formatting) in the inbound adapter layer
 * while delegating persistence to ports.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlackManagerSlashService {

  private final IncidentPersistencePort incidentPersistencePort;
  private final EncryptionAdapter encryptionAdapter;
  private final UserInfoPort userInfoPort;
  private final IncidentInboundPort incidentInboundPort;

  public String closeIncident(SlashCommandRequest request) {
    String userId = request.getUserId();
    String channelId = request.getChannelId();
    String text = request.getText();

    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Reason for closing the incident must be provided.");
    }
    String reason = text;

    // process incident closure asynchronously
    CompletableFuture.runAsync(
        () -> {
          try {
            CloseIncidentCommand command = new CloseIncidentCommand(userId, channelId, reason);

            incidentInboundPort.closeIncident(command);
          } catch (Exception e) {
            System.err.println("Error processing incident closure: " + e.getMessage());
            e.printStackTrace();
          }
        });
    return "✅ Incident closed successfully.";
  }

  public String viewIncidents(SlashCommandRequest request) {
    // Authorize
    boolean isAdmin = userInfoPort.userIsAdmin(request.getUserId());
    log.debug("User {} is admin: {}", request.getUserId(), isAdmin);
    if (!isAdmin) {
      return "❌ You are not authorized to launch this command.";
    }

    Status status = parseStatusOrDefault(request.getText());
    List<IncidentEntity> incidents = incidentPersistencePort.findAllByStatus(status);
    return formatIncidentsList(incidents, status);
  }

  public String assignIncident(SlashCommandRequest request) {
    // Authorize
    boolean isAdmin = userInfoPort.userIsAdmin(request.getUserId());
    log.debug("User {} is admin: {}", request.getUserId(), isAdmin);
    if (!isAdmin) {
      return "❌ You are not authorized to launch this command.";
    }

    String text = request.getText();
    if (text == null || text.trim().isEmpty()) {
      return "❌ Usage: `/assign <developer-id> <incident-id>`\n"
          + "Example: `/assign U12345678 550e8400-e29b-41d4-a716-446655440000`";
    }

    String[] parts = text.trim().split("\\s+");
    if (parts.length < 2) {
      return "❌ Invalid format. Usage: `/assign <developer-id> <incident-id>`\n"
          + "Example: `/assign U12345678 550e8400-e29b-41d4-a716-446655440000`";
    }

    String developerId = parts[0];
    String incidentIdStr = parts[1];

    UUID incidentId;
    try {
      incidentId = UUID.fromString(incidentIdStr);
    } catch (IllegalArgumentException e) {
      return "❌ Invalid incident ID format. Please provide a valid UUID.";
    }

    Optional<IncidentEntity> incidentOpt =
        incidentPersistencePort.assignToDeveloper(incidentId, developerId);

    if (incidentOpt.isEmpty()) {
      return "❌ Incident not found with ID: " + incidentIdStr;
    }

    IncidentEntity incident = incidentOpt.get();
    return String.format(
        "✅ Successfully assigned incident `%s` to <@%s>\n"
            + "• *Status:* %s\n"
            + "• *Severity:* %s",
        incident.getId(), developerId, incident.getStatus(), incident.getSeverity());
  }

  private Status parseStatusOrDefault(String text) {
    Status status = Status.OPEN;
    if (text != null && !text.trim().isEmpty()) {
      try {
        status = Status.valueOf(text.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        status = Status.OPEN;
      }
    }
    return status;
  }

  private String formatIncidentsList(List<IncidentEntity> incidents, Status status) {
    if (incidents.isEmpty()) {
      return String.format("📋 No incidents found with status: *%s*", status);
    }

    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "📋 *Incidents with status: %s* (Showing last %d)\n\n", status, incidents.size()));

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    for (IncidentEntity incident : incidents) {
      String reporterEncrypted =
          incident.getReporter() != null ? incident.getReporter().getReporterId() : null;
      String reporterId =
          reporterEncrypted != null ? encryptionAdapter.decrypt(reporterEncrypted) : "unknown";
      String reporterDisplay = "unknown".equals(reporterId) ? reporterId : "<@" + reporterId + ">";

      String assignee =
          incident.getAssignee() == null || incident.getAssignee().isBlank()
              ? "Pending"
              : incident.getAssignee();
      String assigneeDisplay = ("Pending".equals(assignee)) ? "Pending" : "<@" + assignee + ">";

      sb.append(String.format("• *ID:* `%s`\n", incident.getId()));
      sb.append(String.format("  *Reporter:* %s\n", reporterDisplay));
      sb.append(String.format("  *Status:* %s\n", incident.getStatus()));
      sb.append(String.format("  *Severity:* %s\n", incident.getSeverity()));
      sb.append(String.format("  *Assignee:* %s\n", assigneeDisplay));
      sb.append(
          String.format(
              "  *Created:* %s\n",
              incident.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).format(formatter)));

      String summary = incident.getSummary();
      if (summary != null) {
        String preview = summary.length() > 100 ? summary.substring(0, 100) + "..." : summary;
        sb.append(String.format("  *Summary:* %s\n", preview));
      }

      sb.append("\n");
    }

    return sb.toString();
  }
}
