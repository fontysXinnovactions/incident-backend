package com.innovactions.incident.port.outbound;

import com.innovactions.incident.domain.model.Incident;
import com.innovactions.incident.domain.model.Platform;

public interface IncidentBroadcasterPort {
    String initSlackDeveloperWorkspace(Incident incident, Platform platform);
    void updateIncidentToDeveloper(Incident incident, String channelId);
    void warnUserOfUnlinkedIncident(String reporterId);
    void askUserForMoreInfo(String reporterId);
}
