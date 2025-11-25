package com.innovactions.incident.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Incident {

  @EqualsAndHashCode.Include @ToString.Include private final UUID id = UUID.randomUUID();

  @NonNull private final String reporterId;

  @NonNull private final String reporterName;

  @NonNull private String details;

  /**
   * List of conversation messages.
   */
//  private final List<Message> messages = new ArrayList<>();

  @NonNull private Severity severity;

  private String aiSummary; // using Ai get the context of the message and return summary

  @NonNull private String assignee;

  private final Instant reportedAt = Instant.now();

  private Status status = Status.OPEN;

  public boolean escalate() {
    return severity
        .next()
        .map(
            next -> {
              this.severity = next;
              return true;
            })
        .orElse(false);
  }

  public void resolve() {
    this.status = Status.RESOLVED;
  }

  public void reassign(String newAssignee) {
    this.assignee = newAssignee;
  }

  public void updateDetails(String newDetails) {
    this.details = newDetails;
  }

    /**
     * Add a conversation message to the list.
     */
//    public void addMessage(Message message) {
//        messages.add(message);
//    }
  public String summary() {
    return "📢 Incident ["
        + severity
        + "] — Assigned to "
        + assignee
        + " | Reporter: "
        + reporterName
        + " | Status: "
        + status
        + " | Details: "
        + details;
  }
}
