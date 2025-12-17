CREATE TABLE incidents
(
    id               UUID NOT NULL,
    reporter_id      VARCHAR(255),
    details          VARCHAR(255),
    slack_channel_id VARCHAR(255),
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_incidents PRIMARY KEY (id)
);