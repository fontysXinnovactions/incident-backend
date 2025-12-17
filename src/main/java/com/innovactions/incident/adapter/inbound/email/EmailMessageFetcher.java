package com.innovactions.incident.adapter.inbound.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovactions.incident.adapter.inbound.email.model.EmailMessage;
import com.microsoft.aad.msal4j.*;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailMessageFetcher {

  @Value("${graph.client.id}")
  private String clientId;

  @Value("${graph.client.secret}")
  private String clientSecret;

  @Value("${graph.tenant.id}")
  private String tenantId;

  private static final String GRAPH_BASE = "https://graph.microsoft.com/v1.0";
  private static final Set<String> SCOPE = Set.of("https://graph.microsoft.com/.default");

  private String accessToken;

  @PostConstruct
  public void init() {
    this.accessToken = acquireAccessToken();
  }

  private String acquireAccessToken() {
    try {
      String authority = "https://login.microsoftonline.com/" + tenantId;

      ConfidentialClientApplication app =
          ConfidentialClientApplication.builder(
                  clientId, ClientCredentialFactory.createFromSecret(clientSecret))
              .authority(authority)
              .build();

      ClientCredentialParameters parameters = ClientCredentialParameters.builder(SCOPE).build();

      IAuthenticationResult result = app.acquireToken(parameters).join();

      log.info("Application access token obtained from Microsoft Graph");
      return result.accessToken();

    } catch (Exception e) {
      throw new RuntimeException("Error while acquiring app-only access token", e);
    }
  }

  public EmailMessage fetchMessageDetails(String messageId) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(
                  URI.create(
                      GRAPH_BASE
                          + "/users/automatedincident@outlook.com/messages/"
                          + messageId
                          + "?$select=subject,from,receivedDateTime,bodyPreview"))
              .header("Authorization", "Bearer " + accessToken)
              .header("Accept", "application/json")
              .GET()
              .build();

      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return new ObjectMapper().readValue(response.body(), EmailMessage.class);
      }

      throw new RuntimeException(
          "Graph returned " + response.statusCode() + ": " + response.body());

    } catch (Exception e) {
      throw new RuntimeException("Error while fetching email details", e);
    }
  }
}
