ALTER TABLE incidents
    ADD summary VARCHAR(255);

UPDATE incidents
SET summary = details;

ALTER TABLE incidents
    DROP COLUMN details;