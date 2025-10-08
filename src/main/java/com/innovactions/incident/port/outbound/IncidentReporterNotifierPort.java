package com.innovactions.incident.port.outbound;

public interface IncidentReporterNotifierPort {
    void notifyReporter(String reporterId, String reason);
    String getPlatformName();
}
