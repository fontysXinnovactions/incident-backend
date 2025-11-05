ALTER TABLE incidents
    RENAME COLUMN reporter_id TO reporter_ref;

ALTER TABLE incidents
ALTER COLUMN reporter_ref TYPE UUID
    USING reporter_ref::uuid;

CREATE TABLE reporters
(
    id          UUID         NOT NULL,
    reporter_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_reporters PRIMARY KEY (id)
);

ALTER TABLE reporters
    ADD CONSTRAINT uc_reporters_reporter UNIQUE (reporter_id);

ALTER TABLE incidents
    ADD CONSTRAINT fk_incidents_reporter
    FOREIGN KEY (reporter_ref)
    REFERENCES reporters (id)
    ON DELETE CASCADE;