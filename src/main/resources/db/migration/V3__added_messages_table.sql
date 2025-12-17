CREATE TABLE messages
(
    id           UUID NOT NULL,
    incident_ref UUID NOT NULL,
    sent_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_INCIDENT_REF FOREIGN KEY (incident_ref) REFERENCES incidents (id);