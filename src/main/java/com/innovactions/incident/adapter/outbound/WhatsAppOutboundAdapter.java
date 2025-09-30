package com.innovactions.incident.adapter.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class WhatsAppOutboundAdapter {
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

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,  // recipient phone in international format, no "+"
                "type", "text",
                "text", Map.of("body", message)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            log.info("✅ WhatsApp message sent. Response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage(), e);
        }
    }
}
