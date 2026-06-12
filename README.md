# FieldSync

A multi-tenant REST API and (eventually) offline-sync engine for a mobile
field-data app — built in Kotlin and Spring Boot. Field users log visits and
records on their phones, often offline, then sync when they reconnect.

This repo is built in milestones (see [Roadmap](#roadmap)). **You are at M1:**
a runnable service with a `Visit` resource, PostgreSQL persistence, and
Flyway-managed schema.

## Stack

| Layer       | Choice                                             |
|-------------|----------------------------------------------------|
| Language    | Kotlin                                             |
| Framework   | Spring Boot 3 (Web, Data JPA, Validation, Actuator)|
| Database    | PostgreSQL                                          |
| Migrations  | Flyway                                              |
| API docs    | OpenAPI / Swagger (springdoc)                      |
| Build       | Gradle (Kotlin DSL)                                |
| Tests       | JUnit 5 + Testcontainers                           |
| Container   | Docker                                             |

## Architecture

```
 Mobile client  (simulated here via Swagger UI / requests.http)
        |   HTTPS + JSON
        v
 +-------------------------------------------+
 | Spring Boot API                           |
 |   Controller -> Service -> Repository     |
 |   ApiExceptionHandler (RFC 7807 errors)   |
 |   [M2] JWT auth + per-org tenant scoping  |
 |   [M3] Sync service (idempotent + version |
 |         conflict detection)               |
 +----------------------+--------------------+
                        | JPA
                        v
                 PostgreSQL  (Flyway-managed schema)
```

## Prerequisites

- JDK 17+ (`java -version`)
- Docker (for Postgres and for running the Testcontainers tests)
- No local Gradle needed once the wrapper is generated (see next step)

## First-time setup: generate the Gradle wrapper

This scaffold ships without the wrapper binary. Generate it once — either:

- **IntelliJ IDEA:** open the folder; it imports the Gradle project and creates
  the wrapper automatically. (Recommended — you get a full IDE for the project.)
- **CLI with a local Gradle:** `gradle wrapper --gradle-version 8.7`

After that, use `./gradlew` for everything below.

## Run it locally

```bash
# 1. start Postgres
docker compose up -d

# 2. run the app (Flyway applies V1__init.sql on startup)
./gradlew bootRun
```

Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health:     http://localhost:8080/actuator/health
- Or fire the sample calls in `requests.http` (VS Code REST Client / IntelliJ HTTP client).

Quick smoke test with curl:

```bash
curl -s -X POST http://localhost:8080/api/visits \
  -H 'Content-Type: application/json' \
  -d '{"clientId":"c-1","customerName":"Acme Clinic","visitedAt":"2026-06-12T15:00:00Z"}'

curl -s 'http://localhost:8080/api/visits?size=20'
```

## Run the tests

```bash
./gradlew test
```

The integration test spins up a real Postgres in Docker via Testcontainers,
applies the Flyway migration, and exercises the API through the full stack.

## Project layout

```
src/main/kotlin/com/fieldsync/
  FieldSyncApplication.kt        # entrypoint
  common/                        # cross-cutting: error handling
  visit/                         # the Visit feature (entity, repo, service, controller, dtos)
src/main/resources/
  application.yml                # config (env-overridable DB settings)
  db/migration/V1__init.sql      # Flyway schema
src/test/kotlin/...              # Testcontainers integration test
```

## Design notes (talk about these in interviews)

- **Flyway owns the schema; Hibernate is set to `validate`.** Schema changes are
  explicit, versioned SQL — not magic from `ddl-auto`. This is how real teams
  run JPA in production.
- **The `visits` table already carries `org_id`, `client_id`, and a `version`
  column** even though M1 doesn't use them yet. `org_id` is the tenant boundary
  (M2); the unique `(org_id, client_id)` index is what will make sync
  **idempotent** (M3); `@Version` gives optimistic locking for **conflict
  detection** (M3). The data model is built forward on purpose.
- **Errors return RFC 7807 ProblemDetail** responses, including structured field
  errors on validation failures.
- **`open-in-view: false`** — no lazy-loading surprises leaking out of the
  transaction; the boundary is the service layer.

## Roadmap

- [x] **M1 — Runnable skeleton.** Visit CRUD, Postgres, Flyway, validation,
      error handling, Swagger, one Testcontainers test.
- [ ] **M2 — Auth + multi-tenancy + RBAC.** JWT login, `org_id` from the token,
      query-level tenant scoping, `REP` / `MANAGER` roles, an `orgs` + `users`
      schema (V2 migration).
- [ ] **M3 — Sync engine.** `POST /api/sync` batch endpoint: client-generated
      ids, idempotent upserts on `(org_id, client_id)`, version-based conflict
      detection, per-item results (`applied` / `conflict` / `rejected`).
- [ ] **M4 — Audit log + more tests.** Append-only change log; broaden coverage.
- [ ] **M5 — Ship it.** Dockerized deploy to Fly.io/Render, GitHub Actions CI
      (test on PR, deploy on merge), Actuator metrics.

## Next step (M2 starting point)

Add `spring-boot-starter-security`, a `users`/`orgs` schema as `V2__auth.sql`,
a JWT filter, and replace the `demoOrg` constant in `VisitController` with the
org id pulled from the authenticated principal. Everything else is already
shaped to receive it.
