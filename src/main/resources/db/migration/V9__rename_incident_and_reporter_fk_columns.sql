-- Drop old FKs
ALTER TABLE messages DROP CONSTRAINT fk_messages_on_incident_ref;
ALTER TABLE incidents DROP CONSTRAINT fk_incidents_reporter;

-- Add new columns
ALTER TABLE messages ADD incident_id UUID;
ALTER TABLE incidents ADD reporter_id UUID;

-- Transfer existing data to new columns
UPDATE messages SET incident_id = incident_ref;
UPDATE incidents SET reporter_id = reporter_ref;

ALTER TABLE messages ALTER COLUMN incident_id SET NOT NULL;
ALTER TABLE incidents ALTER COLUMN reporter_id SET NOT NULL;

-- Add new FKs
ALTER TABLE incidents
    ADD CONSTRAINT fk_incidents_on_reporter FOREIGN KEY (reporter_id) REFERENCES reporters (id);

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_on_incident FOREIGN KEY (incident_id) REFERENCES incidents (id);

-- Remove old columns
ALTER TABLE messages DROP COLUMN incident_ref;
ALTER TABLE incidents DROP COLUMN reporter_ref;
