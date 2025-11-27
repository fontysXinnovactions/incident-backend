ALTER TABLE incidents
    DROP CONSTRAINT fk_incidents_reporter;

ALTER TABLE messages
    DROP CONSTRAINT fk_messages_on_incident_ref;

ALTER TABLE messages
    ADD incident_id UUID;

ALTER TABLE messages
    ALTER COLUMN incident_id SET NOT NULL;

ALTER TABLE incidents
    ADD reporter_id UUID;

ALTER TABLE incidents
    ADD CONSTRAINT FK_INCIDENTS_ON_REPORTER FOREIGN KEY (reporter_id) REFERENCES reporters (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_INCIDENT FOREIGN KEY (incident_id) REFERENCES incidents (id);

ALTER TABLE messages
    DROP COLUMN incident_ref;

ALTER TABLE incidents
    DROP COLUMN reporter_ref;