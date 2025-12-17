package com.innovactions.incident.domain.model;

import lombok.Value;

@Value
public class IncidentClassification {
    Severity severity;
    String summary;
}
