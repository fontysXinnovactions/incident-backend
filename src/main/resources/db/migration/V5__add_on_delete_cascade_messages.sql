-- Drop the old constraint (if exists)
ALTER TABLE messages
DROP CONSTRAINT IF EXISTS FK_MESSAGES_ON_INCIDENT_REF;

-- Add the same constraint, now with cascade
ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_INCIDENT_REF
        FOREIGN KEY (incident_ref)
            REFERENCES incidents (id)
            ON DELETE CASCADE;