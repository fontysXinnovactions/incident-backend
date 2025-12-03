package com.innovactions.incident.adapter.outbound.Slack;

import com.innovactions.incident.port.outbound.UserInfoPort;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.UsersInfoResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/** Slack adapter for retrieving user information via the Slack Web API. */
@Slf4j
public class SlackUserInfoAdapter implements UserInfoPort {

  private final String botToken;

  public SlackUserInfoAdapter(String botToken) {
    this.botToken = botToken;
  }

  /** Returns a "is_admin" boolean for the given Slack user ID. */
  @Override
  public boolean userIsAdmin(String userId) {
    try {
      UsersInfoResponse res =
          Slack.getInstance().methods(botToken).usersInfo(req -> req.user(userId));
      if (!res.isOk()) {
        log.warn("Failed to fetch user info for {}: {}", userId, res.getError());
        return false;
      }
      return res.getUser().isAdmin();
    } catch (IOException | SlackApiException e) {
      log.error("Error calling users.info for {}: {}", userId, e.getMessage(), e);
      return false;
    }
  }
}
