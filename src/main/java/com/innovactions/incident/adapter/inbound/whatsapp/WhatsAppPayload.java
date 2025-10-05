package com.innovactions.incident.adapter.inbound.whatsapp;

import lombok.Data;

import java.util.List;

@Data
public class WhatsAppPayload {
    private List<Entry> entry;
    @Data
    public static class Entry {
        private List<Change> changes;
    }
    @Data
    public static class Change {
        private Value value;
    }
    @Data
    public static class Value {
        private List<Contact> contacts;
        private List<Message> messages;
    }
    @Data
    public static class Contact {
        private Profile profile;
    }
    @Data
    public static class Profile {
        private String name;
    }
    @Data
    public static class Message {
        private String from;       // user’s WhatsApp ID (phone number)
        private String timestamp;  // WhatsApp’s actual message timestamp (epoch seconds)
        private Text text;
    }
    @Data
    public static class Text {
        private String body;       // message content
    }
}
