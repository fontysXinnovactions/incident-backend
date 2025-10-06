package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.application.command.CreateIncidentCommand;
import com.innovactions.incident.port.inbound.IncidentInboundPort;
import com.slack.api.Slack;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.event.AppMentionEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;

@Slf4j
public class SlackCreateIncident {

    private final IncidentInboundPort incidentInboundPort;

    public SlackCreateIncident(IncidentInboundPort incidentInboundPort) {
        this.incidentInboundPort = incidentInboundPort;
    }

    public void handleIncomingIncident(AppMentionEvent event, EventContext context) throws SlackApiException, IOException {

        String reporterId = event.getUser();
        String rawText = event.getText();

        // Slack message by default are prepended with a <@----> user id, we remove this for a clean message.
        String cleanText = rawText.replaceAll("<@[A-Z0-9]+(?:\\|[^>]+)?>", "").trim();

        String reporterName = reporterId;

        // Request the username from Slack
        try {
            // Explicitly pass a builder object for more fine control, needed for testing.
            UsersInfoResponse info = Slack
                    .getInstance()
                    .methods()
                    .usersInfo(
                            UsersInfoRequest
                                    .builder()
                                    .user(reporterId)
                                    .build()
                    );
            if (info.isOk() && info.getUser() != null && info.getUser().getProfile() != null) {
                reporterName = info.getUser().getProfile().getRealName();
            }
        } catch (IOException | com.slack.api.methods.SlackApiException e) {
            log.error("Slack API error resolving user info: {}", e.getMessage());
        }

        CreateIncidentCommand command = new CreateIncidentCommand(
                reporterId,
                reporterName,
                cleanText,
                Instant.now()
        );

        incidentInboundPort.handle(command);
    }
}
