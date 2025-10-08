package com.innovactions.incident.adapter.inbound.whatsapp;

import lombok.Data;
import java.util.List;

/**
 * Represents the JSON payload sent by the WhatsApp Business API webhook
 * whenever a new message or event occurs.
 *<p>
 * This structure follows the official:
 * <a href="https://developers.facebook.com/docs/whatsapp/cloud-api/webhooks/components/">Meta WhatsApp Cloud API format:</a>
 */
@Data
public class WhatsAppPayload {

    /**
     * A list of entries, each entry corresponds to a specific
     * WhatsApp Business Account event. Typically, there is only one entry.
     */
    private List<Entry> entry;

    @Data
    public static class Entry {
        /**
         * Each entry can contain one or more changes.
         * A change describes what happened (e.g., a new message was received).
         */
        private List<Change> changes;
    }

    @Data
    public static class Change {
        /**
         * The core of the webhook event.
         * The 'value' object holds the actual message or contact information.
         */
        private Value value;
    }

    @Data
    public static class Value {
        /**
         * List of contacts associated with the message.
         * Usually contains a single contact, the sender of the message.
         */
        private List<Contact> contacts;

        /**
         * List of messages received in this event.
         * Usually contains a single message, but multiples are possible
         * if WhatsApp batches them.
         */
        private List<Message> messages;
    }

    @Data
    public static class Contact {
        /**
         * The sender’s profile information, such as their display name.
         */
        private Profile profile;
    }

    @Data
    public static class Profile {
        /**
         * The user’s visible WhatsApp name, as set in their app profile.
         * Example: "John Doe"
         */
        private String name;
    }

    @Data
    public static class Message {
        /**
         * The sender’s WhatsApp ID, this is usually their phone number in
         * international format (e.g. "31612345678").
         */
        private String from;

        /**
         * The Unix timestamp (in seconds) when the message was sent.
         * Provided as a string by WhatsApp, should be parsed to a long or Instant.
         */
        private String timestamp;

        /**
         * The textual content of the message.
         */
        private Text text;
    }

    @Data
    public static class Text {
        /**
         * The actual message text body sent by the user.
         */
        private String body;
    }
}
