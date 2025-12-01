package com.innovactions.incident.adapter.inbound.email;

import com.innovactions.incident.adapter.inbound.email.mapper.EmailIncidentCommandMapper;
import com.innovactions.incident.adapter.inbound.email.model.EmailMessage;

import com.innovactions.incident.application.command.CreateIncidentCommand;

import com.innovactions.incident.port.inbound.IncidentInboundPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmailInboundAdapter {

    private final EmailMessageFetcher fetcher;
    private final IncidentInboundPort incidentInboundPort;

    public EmailInboundAdapter(EmailMessageFetcher fetcher, IncidentInboundPort incidentInboundPort) {
        this.fetcher = fetcher;
        this.incidentInboundPort = incidentInboundPort;
    }

    @SuppressWarnings("unchecked")
    public void processNotification(Map<String, Object> payload) {

        var values = (List<Map<String, Object>>) payload.get("value");
        if (values == null || values.isEmpty()) {
            return;
        }

        for (var item : values) {

            var resourceData = (Map<String, Object>) item.get("resourceData");
            if (resourceData == null) continue;

            String messageId = (String) resourceData.get("id");
            if (messageId == null) continue;

            try {
                EmailMessage email = fetcher.fetchMessageDetails(messageId);

                CreateIncidentCommand command = EmailIncidentCommandMapper.map(email);

                incidentInboundPort.reportIncident(command);

            } catch (Exception e) {
                log.error("Error while processing email notification", e);
            }
        }
    }
}
