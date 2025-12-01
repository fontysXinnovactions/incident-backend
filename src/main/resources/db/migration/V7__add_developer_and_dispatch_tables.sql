CREATE TABLE developers
(
    id       UUID NOT NULL,
    name     VARCHAR(255),
    slack_id VARCHAR(255),
    CONSTRAINT pk_developers PRIMARY KEY (id)
);

CREATE TABLE dispatch
(
    developer_id UUID NOT NULL,
    incident_id  UUID NOT NULL,
    assigned_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_dispatch PRIMARY KEY (developer_id, incident_id)
);

ALTER TABLE dispatch
    ADD CONSTRAINT FK_DISPATCH_ON_DEVELOPER FOREIGN KEY (developer_id) REFERENCES developers (id) ON DELETE CASCADE;

ALTER TABLE dispatch
    ADD CONSTRAINT FK_DISPATCH_ON_INCIDENT FOREIGN KEY (incident_id) REFERENCES incidents (id) ON DELETE CASCADE;

