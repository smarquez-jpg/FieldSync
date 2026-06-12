CREATE TABLE visits (
    id            UUID PRIMARY KEY,
    client_id     VARCHAR(100) NOT NULL,
    org_id        UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    notes         TEXT,
    visited_at    TIMESTAMPTZ NOT NULL,
    version       BIGINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One row per (org, client_id): the uniqueness that makes sync idempotent in M3.
CREATE UNIQUE INDEX ux_visits_org_client ON visits (org_id, client_id);
CREATE INDEX ix_visits_org ON visits (org_id);
