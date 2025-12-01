package com.innovactions.incident.application;

import com.innovactions.incident.domain.model.Incident;

public record IncidentContext(Incident incident, String slackChannelId) {}
