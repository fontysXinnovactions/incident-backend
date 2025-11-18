package com.innovactions.incident.adapter.inbound.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovactions.incident.adapter.inbound.email.model.EmailMessage;
import com.microsoft.aad.msal4j.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@Slf4j
@Component
public class EmailMessageFetcher {

    @Value("${graph.client-id}")
    private String clientId;

    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final Set<String> SCOPE = Set.of("https://graph.microsoft.com/Mail.Read");
    private static final String GRAPH_BASE = "https://graph.microsoft.com/v1.0";

    private String accessToken;

    @PostConstruct
    public void init() {
        this.accessToken = acquireAccessToken();
    }

    private String acquireAccessToken() {
        try {
            PublicClientApplication app = PublicClientApplication.builder(clientId)
                    .authority(AUTHORITY)
                    .build();

            DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters
                    .builder(SCOPE, (DeviceCode deviceCode) -> log.info(deviceCode.message()))
                    .build();

            IAuthenticationResult result = app.acquireToken(parameters).join();
            log.info("Access token obtained from Microsoft Graph");
            return result.accessToken();
        } catch (Exception e) {
            throw new RuntimeException("Error while receiving Microsoft Graph token", e);
        }
    }

    public EmailMessage fetchMessageDetails(String messageId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GRAPH_BASE + "/me/messages/" + messageId + "?$select=subject,from,receivedDateTime,bodyPreview"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            log.error("Graph status: {}", response.statusCode());
            log.error("Graph response: {}", response.body());


            if (response.statusCode() == 200) {
                EmailMessage message = new ObjectMapper().readValue(response.body(), EmailMessage.class);
                log.info("Parsed message: subject={}, from={}",
                        message.getSubject(),
                        message.getFrom().getEmailAddress().getAddress());
                return message;
            } else {
                log.error("Graph call failed with status {} and body {}", response.statusCode(), response.body());
                throw new RuntimeException("Graph answered with status " + response.statusCode());
            }



        } catch (Exception e) {
            throw new RuntimeException("Error while receiving email details", e);
        }
    }
}
