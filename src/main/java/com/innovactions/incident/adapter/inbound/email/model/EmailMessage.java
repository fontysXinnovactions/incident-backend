package com.innovactions.incident.adapter.inbound.email.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmailMessage {
    private String id;
    private String subject;
    private String receivedDateTime;
    private String bodyPreview;
    private From from;

    @Data
    public static class From {
        private EmailAddress emailAddress;
    }

    @Data
    public static class EmailAddress {
        private String name;
        private String address;
    }
}
