package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.Incident;

public interface IncidentBroadcasterPort {
    String broadcast(Incident incident, String platform);//, String sourcePlatform
    void updateBroadcast(Incident incident, String channelId);
}
