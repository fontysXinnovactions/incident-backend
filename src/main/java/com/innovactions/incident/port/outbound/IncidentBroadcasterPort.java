package com.innovactions.incident.port.outbound;

import com.innovactions.incident.application.Platform;
import com.innovactions.incident.domain.model.Incident;

public interface IncidentBroadcasterPort {
    String initSlackDeveloperWorkspace(Incident incident, Platform platform);
    void updateIncidentToDeveloper(Incident incident, String channelId);
}
