package com.innovactions.incident.adapter.outbound.Slack;

import com.innovactions.incident.port.outbound.BotMessagingPort;
import com.innovactions.incident.port.outbound.IncidentReporterNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SlackIncidentReporterNotifierAdapter implements IncidentReporterNotifierPort {

  private final BotMessagingPort reporterBotMessagingPort;

  @Override
  public void notifyReporter(String reporterId, String reason) {
    reporterBotMessagingPort.sendMessage(
        reporterId, "✅ Your reported incident has been closed.\nReason: " + reason);
  }

  @Override
  public String getPlatformName() {
    return "slack";
  }
}
