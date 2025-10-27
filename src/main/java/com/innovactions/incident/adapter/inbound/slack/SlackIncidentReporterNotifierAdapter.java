package com.innovactions.incident.adapter.inbound.slack;

import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class SlackIncidentReporterNotifierAdapter implements IncidentReporterNotifierPort {

    private final String botTokenA;

    @Override
    public void notifyReporter(String reporterId, String reason) {
        try {
            ChatPostMessageResponse response = Slack.getInstance().methods(botTokenA)
                    .chatPostMessage(req -> req
                            .channel(reporterId)
                            .text("✅ Your reported incident has been closed.\nReason: " + reason)
                    );

            if (!response.isOk()) {
                log.error("Failed to notify Slack reporter {}: {}", reporterId, response.getError());
            } else {
                log.info("Notified Slack reporter {} about incident closure", reporterId);
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error notifying Slack reporter {}: {}", reporterId, e.getMessage(), e);
        }
    }

    @Override
    public String getPlatformName() {
        return "slack";
    }
}
