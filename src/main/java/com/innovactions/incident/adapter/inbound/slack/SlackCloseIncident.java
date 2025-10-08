package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.application.command.CloseIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackCloseIncident {

  private final IncidentInboundPort incidentInboundPort;

  public SlackCloseIncident(IncidentInboundPort incidentInboundPort) {
    this.incidentInboundPort = incidentInboundPort;
  }

  public void handle(SlashCommandRequest req, SlashCommandContext context) {
    String developerUserId = req.getPayload().getUserId();
    String channelId = req.getPayload().getChannelId();
    String reason =
        req.getPayload().getText() != null ? req.getPayload().getText() : "No reason provided";

    CloseIncidentCommand command = new CloseIncidentCommand(developerUserId, channelId, reason);

    // process incident closure asynchronously
    CompletableFuture.runAsync(
        () -> {
          try {
            incidentInboundPort.closeIncident(command);
          } catch (Exception e) {
            log.error("Error processing incident closure: {}", e.getMessage(), e);
          }
        });
  }
}
