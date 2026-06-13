CREATE TABLE orgs (
                      id         UUID PRIMARY KEY,
                      name       VARCHAR(255) NOT NULL,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
                       id            UUID PRIMARY KEY,
                       org_id        UUID NOT NULL REFERENCES orgs(id),
                       email         VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(50)  NOT NULL,
                       created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_users_email ON users (email);

-- Seed the demo org so the visits you created in M1 still belong to a real org.
INSERT INTO orgs (id, name)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Org');

-- Now that an orgs table exists, enforce that every visit points to a real org.
ALTER TABLE visits
    ADD CONSTRAINT fk_visits_org FOREIGN KEY (org_id) REFERENCES orgs(id);