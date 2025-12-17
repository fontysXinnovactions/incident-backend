package com.innovactions.incident.adapter.inbound.slack;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic model for Slack slash command payload that we care about in the inbound adapter.
 *
 * <p>Stays in the adapter layer so the core application/domain is not tied to Slack-specific
 * parameters.
 */
@Data
@AllArgsConstructor
public class SlashCommandRequest {

  private String userId;
  private String channelId;
  private String text;
}
