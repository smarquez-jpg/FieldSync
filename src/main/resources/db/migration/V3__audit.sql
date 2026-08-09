CREATE TABLE audit_log (
                           id          UUID PRIMARY KEY,
                           org_id      UUID NOT NULL,
                           user_id     UUID,
                           action      VARCHAR(20) NOT NULL,   -- CREATE / UPDATE / DELETE
                           entity_type VARCHAR(50) NOT NULL,   -- e.g. "Visit"
                           entity_id   UUID NOT NULL,
                           occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for reading an org's history newest-first.
CREATE INDEX ix_audit_org_time ON audit_log (org_id, occurred_at DESC);