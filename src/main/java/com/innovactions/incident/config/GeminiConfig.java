package com.innovactions.incident.config;

import com.innovactions.incident.adapter.outbound.AI.GeminiIncidentClassifier;
import com.innovactions.incident.port.outbound.SeverityClassifierPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
  @Bean
  public SeverityClassifierPort geminiIncidentClassifier(
      @Value("${gemini.api.key}") String apiKey) {
    return new GeminiIncidentClassifier(apiKey);
  }
}
