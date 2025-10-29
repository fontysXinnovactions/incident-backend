package com.innovactions.incident.port.outbound;

import com.innovactions.incident.application.Platform;

public interface IncidentClosurePort {
    void closeIncident(String devId, String channelId, String reason);
    //FIXME: Remove only testing
    Platform getPlatformName();
}
