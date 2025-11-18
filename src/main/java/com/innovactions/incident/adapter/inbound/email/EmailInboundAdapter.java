package com.innovactions.incident.adapter.inbound.email;

import com.innovactions.incident.adapter.outbound.AI.GeminiIncidentClassifier;
import com.innovactions.incident.adapter.outbound.Slack.SlackBroadcaster;
import com.innovactions.incident.application.Platform;
import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmailInboundAdapter {

    private final EmailMessageFetcher fetcher;
    private final GeminiIncidentClassifier classifier;
    private final SlackBroadcaster slackBroadcaster;

    public EmailInboundAdapter(EmailMessageFetcher fetcher,
                               GeminiIncidentClassifier classifier,
                               SlackBroadcaster slackBroadcaster) {
        this.fetcher = fetcher;
        this.classifier = classifier;
        this.slackBroadcaster = slackBroadcaster;
    }

    @SuppressWarnings("unchecked")
    public void processNotification(Map<String, Object> payload) {
        List<Map<String, Object>> values = (List<Map<String, Object>>) payload.get("value");
        if (values == null) return;

        for (Map<String, Object> item : values) {
            Map<String, Object> resourceData = (Map<String, Object>) item.get("resourceData");
            if (resourceData == null) continue;

            String messageId = (String) resourceData.get("id");
            if (messageId == null) continue;

            try {
                var message = fetcher.fetchMessageDetails(messageId);
                log.info("New mail recieved from {}", message.getFrom().getEmailAddress().getAddress());

                Severity isIncident = classifier.classify(message.getBodyPreview());

                log.info(isIncident.name());

                //  Create domain object
                Incident incident = new Incident(
                        message.getFrom().getEmailAddress().getAddress(), // reporterId
                        message.getFrom().getEmailAddress().getAddress(), // reporterName
                        message.getBodyPreview(),
                        Severity.MINOR,
                        "slack_bot"
                );

                slackBroadcaster.initSlackDeveloperWorkspace(incident, Platform.EMAIL);
                log.info("Send incident to Slack: {}", incident.summary());
            } catch (Exception e) {
                log.error("error while processing e-mailnotification", e);
            }
        }
    }
}
