ALTER TABLE messages
    ADD content TEXT;

ALTER TABLE messages
    ALTER COLUMN content SET NOT NULL;