package com.innovactions.incident.adapter.outbound.Slack;

/**
 * Represents reporter information extracted from a channel topic.
 *
 * <p>This is a domain value object used by the ChannelAdministrationPort interface and its
 * implementations. It contains the reporter's ID and the platform they used.
 */
public class ReporterInfo {
  public final String reporterId;
  public final String platform;

  public ReporterInfo(String reporterId, String platform) {
    this.reporterId = reporterId;
    this.platform = platform;
  }
}
