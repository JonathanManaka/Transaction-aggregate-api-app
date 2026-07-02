# Transaction Aggregator API

A Spring Boot service that **aggregates transactions from multiple bank sources into a single, normalised, categorised store** and exposes a REST API to query and analyse them. Aggregation is orchestrated durably with [Temporal](https://temporal.io/) and can run on demand or on a schedule.

---

## What it does

1. **Ingest** – raw bank transactions are inserted into a `source_transactions` table via the API (each tagged with a `source`, e.g. `BANK_A` / `BANK_B`).
2. **Aggregate** – a Temporal workflow fetches the raw transactions per source, skips duplicates, **normalises** and **categorises** them, and persists the result into the `transactions` table.
3. **Serve** – REST endpoints expose the aggregated transactions plus category summaries and monthly spending analytics.

```
POST /api/source-transactions ─▶ source_transactions table
                                        │
              Temporal workflow (manual /api/aggregate  OR  every-5-min schedule)
                                        │  fetch → isPresent → normalise → categorise → save
                                        ▼
                                 transactions table ─▶ /api/transactions, /api/summary, /api/analytics
```

---

## Tech stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 4.0.6 (Web MVC, Data JPA, Actuator, Validation) |
| Orchestration | Temporal SDK 1.31.0 |
| Database | PostgreSQL (prod) / H2 (local dev & unit tests) |
| Migrations | Flyway |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (`./mvnw`) |
| Tests | JUnit, Mockito, Testcontainers (Postgres) |
| Container | Docker (`eclipse-temurin:17-jre-alpine`) |

---

## API endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/source-transactions` | Insert one or more raw source transactions (JSON array). |
| `GET`  | `/api/source-transactions` | List all raw source transactions. |
| `POST` | `/api/aggregate` | Manually trigger the aggregation workflow. Returns the number of newly saved transactions. |
| `GET`  | `/api/transactions` | List aggregated transactions. Filter by `category`, `startDate`, `endDate`, `minAmount`, `maxAmount`. |
| `GET`  | `/api/transactions/{id}` | Get a single aggregated transaction. |
| `GET`  | `/api/summary` | Totals grouped by category. |
| `GET`  | `/api/categories` | Category breakdown with counts. |
| `GET`  | `/api/analytics` | Monthly spending trends. |
| `GET`  | `/health` | Liveness check (used by K8s probes). |
| `GET`  | `/actuator/**` | Spring Boot Actuator endpoints. |
| `GET`  | `/swagger-ui.html` | Interactive API documentation. |

### Example: insert source transactions then aggregate

```bash
curl -X POST http://localhost:8080/api/source-transactions \
  -H 'Content-Type: application/json' \
  -d '[
    {"externalId":"BANKA-001","source":"BANK_A","description":"Woolworths Food Purchase","amount":450.99,"currency":"ZAR","transactionDate":"2026-06-30T10:15:00","accountId":"ACC-1001"},
    {"externalId":"BANKB-001","source":"BANK_B","description":"Checkers Groceries","amount":320.75,"currency":"ZAR","transactionDate":"2026-06-30T17:40:00","accountId":"ACC-2001"}
  ]'

curl -X POST http://localhost:8080/api/aggregate
```

> `source` must be exactly `BANK_A` or `BANK_B` — that is what the aggregation fetch filters on.

---

## Data model

- **`source_transactions`** – raw, unprocessed transactions as received from a bank. Unique on `(external_id, source)`.
- **`transactions`** – aggregated, normalised, categorised transactions. Unique on `(external_id, source)`; carries a `category` enum.

Schema is managed by Flyway (`src/main/resources/db/migration`).

---

## Configuration

Set via environment variables (defaults shown where applicable):

| Variable | Purpose |
|---|---|
| `POSTGRES_URL` | JDBC URL for Postgres (prod/default profile). |
| `POSTGRES_USERNAME` | Postgres username. |
| `POSTGRES_PASSWORD` | Postgres password. |
| `TEMPORAL_HOST` | Temporal frontend address (default `localhost:7233`). |

Key application properties:

| Property | Default | Purpose |
|---|---|---|
| `temporal.service-address` | `${TEMPORAL_HOST:localhost:7233}` | Temporal frontend. |
| `temporal.worker.enabled` | `true` | Register the Temporal worker (workflow + activities). |
| `temporal.schedule.enabled` | `true` | Create the "every 5 minutes" aggregation schedule on startup. |

**Profiles**
- *default* – Postgres + Flyway; Temporal worker and schedule enabled.
- `dev` – in-memory H2 (seeded with sample data); Swagger + H2 console enabled.

---

## Running locally

### Option A – app only, no Temporal (quickest)

Runs against in-memory H2 with the worker and schedule turned off, so you can exercise the REST API without a Temporal cluster:

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.arguments="--temporal.worker.enabled=false --temporal.schedule.enabled=false"
```

App: <http://localhost:8080> · Swagger: <http://localhost:8080/swagger-ui.html> · H2 console: <http://localhost:8080/h2-console>

> Note: with the worker disabled, `POST /api/aggregate` will not be processed. Use Option B to exercise the full aggregation flow.

### Option B – full stack with Temporal

1. Start a local Temporal dev server (e.g. `temporal server start-dev`, frontend on `localhost:7233`).
2. Run the app pointed at it (H2 for storage):

```bash
TEMPORAL_HOST=localhost:7233 ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The worker registers on task queue `TransactionAggregatorTaskQueue`, and a schedule (`aggregator-every-5min`) starts the workflow every 5 minutes. Trigger manually any time with `POST /api/aggregate`.

### Option C – against Postgres

```bash
export POSTGRES_URL="jdbc:postgresql://localhost:5432/transac_aggr"
export POSTGRES_USERNAME="postgres"
export POSTGRES_PASSWORD="postgres"
export TEMPORAL_HOST="localhost:7233"
./mvnw spring-boot:run
```

---

## Testing

```bash
# Everything (unit + integration). Integration tests need a working Docker daemon.
./mvnw test

# Unit tests only (skip the Testcontainers Postgres test) – no Docker required
./mvnw test -Dspring.profiles.active=dev -Dtest='!TransactionRepositoryTest'

# Just the Testcontainers integration test
./mvnw test -Dtest=TransactionRepositoryTest
```

**Testcontainers / Docker note (Rancher Desktop):** the integration test spins up `postgres:15` via Testcontainers, which needs the Docker socket. With Rancher Desktop:

```bash
export DOCKER_HOST="unix://$HOME/.rd/docker.sock"
./mvnw test -Dtest=TransactionRepositoryTest
```

---

## Build & Docker

```bash
# Build the JAR
./mvnw clean package

# Build the image (Dockerfile expects the JAR under target/)
docker build -t transac-aggr-api-app:local .

# Run the container
docker run --rm -p 8080:8080 \
  -e POSTGRES_URL=... -e POSTGRES_USERNAME=... -e POSTGRES_PASSWORD=... \
  -e TEMPORAL_HOST=... \
  transac-aggr-api-app:local
```

The image runs as a non-root user (`uid 10000`) and exposes port `8080`.

---

## CI/CD

GitHub Actions (`.github/workflows/`):

- **`docker-image.yml`** – on push to `dev`: build & test → build & push the Docker image (tagged with the 7-char commit SHA) → open a PR against the GitOps repo bumping the deployed image tag (requires the `GITOPS_PAT` secret with Contents + Pull requests write).
- **`codeql.yml`** – CodeQL static analysis for Java.

Deployment is GitOps-driven: merging the auto-generated tag-bump PR triggers an ArgoCD sync.

---

## Project layout

```
src/main/java/org/example/transacaggrapiapp/
├── controller/        REST controllers (transactions, source-transactions, summary, analytics, health)
├── service/           Application services
├── aggregator/        Temporal workflow impl + normaliser
├── categoriser/       Rule-based transaction categoriser
├── TransactionAggregatorActivity/   Temporal activity interface + impl
├── source/            Bank source clients (read from source_transactions)
├── repository/        Spring Data JPA repositories
├── model/             JPA entities & enums
├── TransactionAggregatorWorker.java     Temporal worker registration
├── TransactionAggregatorSchedule.java   Temporal 5-minute schedule
└── InitiateTransactionAggregator.java   Starts the workflow (manual trigger)
```
