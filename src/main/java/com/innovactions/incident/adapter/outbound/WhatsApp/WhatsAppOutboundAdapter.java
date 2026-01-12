package com.innovactions.incident.adapter.outbound.WhatsApp;

import com.innovactions.incident.port.outbound.WhatsAppOutboundPort;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class WhatsAppOutboundAdapter implements WhatsAppOutboundPort {

  //FIXME: remove only for testing quoting
  private static final String HARDCODED_QUOTE_MESSAGE_ID =
          "wamid.HBgLMzE2MTkzMTUyNTMVAgASGCBBQzQ3NENERTc5MEZFNTUzMjNEM0FENjgyOTZGNDJBMQA=";

  @Value("${whatsapp.apiUrl}")
  private String apiUrl; // e.g. https://graph.facebook.com/v20.0

  @Value("${whatsapp.accessToken}")
  private String accessToken; // from Meta App dashboard

  @Value("${whatsapp.phoneNumberId}")
  private String phoneNumberId; // from Meta App dashboard

  private final RestTemplate restTemplate = new RestTemplate();

  public void sendTextMessage(String to, String message) {
    String url = apiUrl + "/" + phoneNumberId + "/messages";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);

    Map<String, Object> payload =
        Map.of(
            "messaging_product",
            "whatsapp",
            "to",
            to, // recipient phone in international format, no "+"
            "type",
            "text",
            "text",
            Map.of("body", message));

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

      log.info("✅ WhatsApp message sent. Response: {}", response.getBody());
    } catch (Exception e) {
      log.error("Failed to send WhatsApp message: {}", e.getMessage(), e);
    }
  }
//FIXME: remove only for testing quoted messages
  public void sendQuotedTextMessageHardcoded(String to, String message) {
    String url = apiUrl + "/" + phoneNumberId + "/messages";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);

    Map<String, Object> payload =
            Map.of(
                    "messaging_product", "whatsapp",
                    "to", to,
                    "type", "text",
                    "context", Map.of("message_id", HARDCODED_QUOTE_MESSAGE_ID),
                    "text", Map.of("body", message)
            );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response =
              restTemplate.postForEntity(url, request, String.class);

      log.info("✅ WhatsApp QUOTED message sent (hardcoded). Response: {}", response.getBody());
    } catch (Exception e) {
      log.error("❌ Failed to send quoted WhatsApp message", e);
    }
  }

}
