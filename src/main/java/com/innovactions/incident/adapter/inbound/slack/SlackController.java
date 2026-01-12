package com.innovactions.incident.adapter.inbound.slack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slack/manager")
@RequiredArgsConstructor
@Slf4j
public class SlackController {

  private final SlackManagerSlashService slackManagerSlashService;

  @PostMapping("/close_incident")
  public String closeIncident(
      @RequestParam String user_id,
      @RequestParam String channel_id,
      @RequestParam(required = true) String text) {
    SlashCommandRequest request = new SlashCommandRequest(user_id, channel_id, text);
    return slackManagerSlashService.closeIncident(request);
  }

  @PostMapping("/view")
  public String viewIncidents(
      @RequestParam String user_id,
      @RequestParam String channel_id,
      @RequestParam(required = false) String text) {
    SlashCommandRequest request = new SlashCommandRequest(user_id, channel_id, text);
    return slackManagerSlashService.viewIncidents(request);
  }

  @PostMapping("/assign")
  public String assignIncident(
      @RequestParam String user_id,
      @RequestParam String channel_id,
      @RequestParam(required = false) String text) {
    SlashCommandRequest request = new SlashCommandRequest(user_id, channel_id, text);
    return slackManagerSlashService.assignIncident(request);
  }
}
