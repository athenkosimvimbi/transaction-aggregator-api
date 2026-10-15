# Transaction Aggregation API

Author: **Athenkosi Mvimbi**

A Spring Boot REST API that aggregates, categorises, and analyses personal financial transactions from multiple source systems.

The application imports transaction data from external providers, automatically categorises transactions using a rule-based engine, and exposes reporting and analytics endpoints for spending insights, income tracking, and financial summaries.

## What it does

| Piece | Description |
|---|---|
| Data sources | Three mock sources behind a `TransactionSource` interface: **bank account**, **credit card**, **mobile wallet**. Data is generated deterministically per customer and month, so re-syncing never creates duplicates. |
| Categorisation | Rule-based engine (`CategorizationService`): 15 categories such as Groceries, Fuel, Utilities, Airtime & data, Salary. Customers can override a category, and overrides are never overwritten by later syncs. |
| Storage | MySQL 8, schema managed by Flyway migrations. Amounts are `DECIMAL(15,2)`, credits positive and debits negative. |
| API | REST under `/api/v1`, documented with Swagger UI. |
| Dashboard | Single page at `/`: summary cards, category / month / merchant / source breakdowns, searchable transaction table, re-categorise and sync buttons. |

## Prerequisites

- **Docker Desktop** (or Docker Engine with the Compose plugin). This is all you need to run it.
- Optional, only for running without Docker: **JDK 21** and **Maven 3.9+**.

## Build and run (recommended: Docker Compose)

From the project folder (the one containing `docker-compose.yml`):

```bash
docker compose up --build
```

This builds the app image, starts MySQL, waits until it is healthy, runs the Flyway migrations
and loads six months of mock transactions for the three seeded customers.
The first build takes a few minutes because Maven downloads dependencies.

Wait for a log line like `Startup sync finished: ... new transactions`, then open the app.

To stop it: press `Ctrl+C`, then `docker compose down`
(add `-v` to also delete the database volume and start fresh).

## How to open it

1. Make sure `docker compose up --build` is running and has finished starting.
2. Open a browser and go to **http://localhost:8080/** for the dashboard.
3. Pick a customer from the dropdown at the top. Use the date pickers and **Apply** to narrow the period.
4. Click **Sync sources** to pull the latest data again, or change a category in the table to correct it.
5. API documentation (Swagger UI): **http://localhost:8080/swagger-ui.html**
6. Health check: **http://localhost:8080/actuator/health**

If port 8080 or 3307 is already used on your machine, change the left-hand number in the `ports`
section of `docker-compose.yml` and use that port in the URLs above.

## Run with plain Docker (no Compose)

```bash
docker network create agg-net
docker run -d --name agg-mysql --network agg-net \
  -e MYSQL_DATABASE=aggregator -e MYSQL_USER=aggregator \
  -e MYSQL_PASSWORD=aggregator -e MYSQL_ROOT_PASSWORD=rootpass mysql:8.4

docker build -t transaction-aggregator .
docker run --rm --name agg-app --network agg-net -p 8080:8080 \
  -e DB_URL="jdbc:mysql://agg-mysql:3306/aggregator?useSSL=false&allowPublicKeyRetrieval=true" \
  -e DB_USER=aggregator -e DB_PASSWORD=aggregator transaction-aggregator
```

MySQL needs about 20 seconds to initialise on first start. If the app exits because it cannot connect, run the last command again.

## Run locally without Docker for the app

Start only the database, then run the app with Maven:

```bash
docker compose up -d mysql      # MySQL is exposed on localhost:3307
mvn spring-boot:run
```

Configuration is read from environment variables (defaults point at the Compose MySQL):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3307/aggregator?useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USER` / `DB_PASSWORD` | `aggregator` / `aggregator` |
| `SYNC_ON_STARTUP` | `true` (set to `false` to start with an empty database) |

## Test

The unit tests need no database:

```bash
mvn test
```

Without a local Maven or JDK, run them in a container:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-21 mvn -B test
```

On Windows PowerShell use `${PWD}` instead of `$PWD`.

Covered: the categorisation rules (including the word-boundary and credit-only edge cases),
the mock sources (repeatable output, unique ids, date window, no zero amounts) and the ingestion logic
(duplicate skipping, bad rows, one failing source not stopping the others, input validation).

You can also try the live API by hand:

```bash
curl http://localhost:8080/api/v1/customers
curl "http://localhost:8080/api/v1/customers/1/summary"
curl "http://localhost:8080/api/v1/customers/1/aggregations/by-category?from=2026-08-01&to=2026-10-14"
curl "http://localhost:8080/api/v1/customers/1/transactions?category=GROCERIES&sortBy=amount&direction=ASC&size=5"
curl -X PATCH http://localhost:8080/api/v1/transactions/1/category \
     -H "Content-Type: application/json" -d '{"category":"SHOPPING"}'
```

## API reference

Base path `/api/v1`. All amounts are ZAR with two decimals. Dates are `yyyy-MM-dd`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/customers` | List customers |
| GET | `/customers/{id}` | One customer |
| GET | `/customers/{id}/transactions` | Search. Filters: `from`, `to`, `category`, `source`, `type` (CREDIT/DEBIT), `minAmount`, `maxAmount`, `q` (text). Paging: `page`, `size` (max 200). Sorting: `sortBy` (bookedOn, amount, merchant, category), `direction` |
| GET | `/transactions/{id}` | One transaction |
| PATCH | `/transactions/{id}/category` | Correct a category, body `{"category":"FUEL"}` |
| GET | `/customers/{id}/summary` | Income, expenses, net cashflow, savings rate |
| GET | `/customers/{id}/aggregations/by-category` | Per category, with share of total spend |
| GET | `/customers/{id}/aggregations/by-month` | Monthly cashflow |
| GET | `/customers/{id}/aggregations/by-source` | Per data source |
| GET | `/customers/{id}/aggregations/top-merchants` | Biggest spend per merchant, `limit` up to 50 |
| POST | `/customers/{id}/sync?months=6` | Pull sources for one customer (1 to 24 months) |
| POST | `/sync?months=6` | Pull sources for every customer |
| GET | `/categories`, `/sources` | Valid codes for dropdowns |

Errors use RFC 7807 problem details, for example a missing customer returns `404` and a bad date range returns `400`.

## Design notes

- **Layers**: controllers (HTTP only), services (business logic), repositories (data access), sources (integrations).
  Adding a real data source means implementing `TransactionSource`, nothing else changes.
- **ZAR only**: one `Money` helper and no currency column, so mixed currencies cannot exist. Money is `BigDecimal`, never floating point.
- **Idempotent sync**: a unique constraint on `(customer_id, source, external_id)` plus an in-code check means repeated syncs only add new rows.
- **Resilient sync**: every source is stored in its own transaction. A failing source is reported as `FAILED` in the response while the others still succeed.
- **Aggregation in SQL**: sums and grouping happen in MySQL, indexed by customer, date, category and merchant.
- **Safe queries**: search filters use the JPA Criteria API (bound parameters), sort fields are whitelisted, LIKE wildcards are escaped.
- **Schema control**: Flyway owns the schema and Hibernate is set to `ddl-auto: none`.
- **Container**: multi-stage Docker build, small JRE runtime image, non-root user.

## Project layout

```
src/main/java/com/mvimbi/aggregator
  config/       Clock bean (Africa/Johannesburg)
  domain/       Entities and enums (Customer, TransactionRecord, Category...)
  repository/   Spring Data repositories and aggregation queries
  source/       TransactionSource interface and the three mock sources
  service/      Categorisation, ingestion, aggregation, search
  web/          Controllers, DTOs, error handling
src/main/resources
  db/migration/ Flyway SQL (schema + seed customers)
  static/       Dashboard (index.html)
src/test/java   Unit tests
Dockerfile, docker-compose.yml
```
