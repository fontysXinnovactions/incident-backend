package com.innovactions.incident.port.outbound;

import com.innovactions.incident.application.Platform;
import com.innovactions.incident.domain.model.Incident;

public interface IncidentBroadcasterPort {
    String broadcast(Incident incident, Platform platform);
    void updateBroadcast(Incident incident, String channelId);
}
