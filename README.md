# Aegis-Mesh

An event-driven microservice system that helps patients find scarce medicines across pharmacies and reserve them for pickup.

During Sri Lanka's 2022 medicine shortage, people travelled from pharmacy to pharmacy looking for medicines they depended on. Aegis-Mesh shows live stock and lets patients reserve it, so that trip only happens once.

The project focuses on real distributed-system problems: each service owns its own database, services coordinate through Kafka, and limited stock is reserved safely when many people ask for it at the same time.

## Services

| Service | Port | Database | Responsibility |
|---|---|---|---|
| [Request Service](services/request-service) | 8080 | `request_db` (:5433) | Patients create, view and cancel medicine requests. Starts the reservation saga and records its outcome. |
| [Stock Service](services/stock-service) | 8081 | `stock_db` (:5434) | Pharmacies, medicine stock and concurrency-safe reservations. Reserves and releases stock in response to request events. |
| [Storefront UI](storefront-ui) | 5173 | – | Patient-facing React app: landing page, login and sign-up. |
| Kafka UI | 8090 | – | Browse topics, messages, consumer groups and lag. |

## Architecture

```text
                 Patient
                    │  REST
                    ▼
            ┌────────────────┐
            │ Request Service│──► request_db
            └────────────────┘
          created │      ▲ reservation-result
        cancelled │      │
                  ▼      │
            ┌────────────────┐
            │  Apache Kafka  │
            └────────────────┘
                  │      ▲
                  ▼      │
            ┌────────────────┐
            │  Stock Service │──► stock_db
            └────────────────┘
```

### Kafka topics

| Topic | Producer | Consumer | Meaning |
|---|---|---|---|
| `aegis.medicine-request.created` | Request Service | Stock Service | A patient asked for medicines; reserve them. |
| `aegis.stock.reservation-result` | Stock Service | Request Service | Stock was `RESERVED` or `REJECTED` (with a reason). |
| `aegis.medicine-request.cancelled` | Request Service | Stock Service | The patient cancelled; release any reserved stock. |
| `<topic>-dlt` | Error handler | – | Messages that still failed after retries, with the exception in the headers. |

All events use the `requestId` as the message key, so every event for one request lands on the same partition and is processed in order.

### Reservation saga

```text
Happy path   create ──created──► reserve ──result: RESERVED──► STOCK_RESERVED
No stock     create ──created──► reject  ──result: REJECTED──► STOCK_REJECTED (+ reason)
Cancel       cancel ──cancelled──► release reserved stock
Late result  cancelled request receives RESERVED ──► publish cancelled again ──► release
```

## Engineering Highlights

### Choreography saga with compensation
There is no distributed transaction across the two databases. Each step is a local transaction, and the services react to each other's events. Cancelling a request is the compensating action: the Stock Service releases what it reserved.

Because `created` and `cancelled` travel on different topics, a cancel can be processed before the reservation exists. When a `RESERVED` result later arrives for a request that is already cancelled, the Request Service publishes the cancel event again, so the stock is never left held for a cancelled request.

### No overselling under concurrency
- Stock rows are locked with pessimistic row locks (`SELECT ... FOR UPDATE`) while a reservation is checked and applied.
- Rows are always locked in the same order (sorted by medicine code), which prevents deadlocks between reservations for several medicines.
- Stock is modelled as `available = onHand - reserved`. Reserving holds stock without removing it, so a cancel or expiry can give it back.

### Idempotent consumers
Kafka delivers at least once, so every handler is safe to run twice:
- **Reserve**: an existing reservation for the `requestId` is returned instead of reserving again. A unique constraint on `request_id` is the last line of defence when two copies are processed at once.
- **Release**: an already released reservation is left as is.
- **Apply result**: only a request that is still waiting for stock can change state; a redelivered result is ignored.

### Retries and dead-letter topics
- Failing listeners are retried with exponential backoff (1s, 2s, 4s), which covers temporary problems such as a database restart.
- A message that still fails is published to `<topic>-dlt` instead of being dropped, with the exception class and message in its headers so it can be inspected and replayed.
- Messages that cannot be deserialized are never retried and go straight to the dead-letter topic with their original bytes.
- Business failures (unknown or inactive pharmacy, not enough stock) are not retried; they become a `REJECTED` result with a reason the patient can read.

### Loosely coupled contracts
Services share a JSON contract, not Java classes. Each service keeps its own copy of the event records, and the result status is read as a string so a new status added by the Stock Service does not break deserialization in the Request Service.

### Verified end to end
The saga was exercised against a real Kafka broker and both databases: reserve, reject for insufficient stock, cancel with stock release, unknown pharmacy, a malformed message (straight to the DLT) and a failing message (three retries with backoff, then the DLT). Both consumer groups finished with zero lag.

## Known Limitations

These are deliberate next steps rather than oversights:

| Limitation | Impact | Planned fix |
|---|---|---|
| Dual write | The database update and the Kafka publish are not atomic, so an event can be lost or sent for a rolled-back change. | Transactional outbox |
| No automated tests yet | The saga was verified manually against running services. | Testcontainers integration tests and a concurrent-reservation test |
| No authentication | Any client can call any API. | Keycloak with `PATIENT`, `PHARMACIST` and `ADMIN` roles |
| Pickup holds never expire | Reserved stock stays held until the request is cancelled. | Scheduled expiry that releases stock |
| Schema managed by Hibernate | `ddl-auto: update` is not safe for production changes. | Flyway migrations |
| Different Spring Boot versions | Request Service uses 3.5, Stock Service uses 4.x. | Align both services |

## Roadmap

- [x] Stock Service consumes `medicine-request.created` and reserves stock
- [x] Idempotent Kafka consumers
- [x] Reservation result closes the saga in the Request Service
- [x] Cancel compensation releases reserved stock
- [x] Retries with exponential backoff and dead-letter topics
- [x] Kafka UI
- [x] Storefront landing, login and sign-up pages
- [ ] Transactional outbox
- [ ] Testcontainers tests, including a concurrent-reservation test
- [ ] Keycloak login with patient, pharmacist and admin roles
- [ ] Central medicine catalogue (active ingredient, strength, form)
- [ ] Search and reservation pages in the storefront; pharmacist and admin dashboards
- [ ] Pickup holds that expire and release stock
- [ ] AI Advisor that suggests nearby pharmacies or same-ingredient medicines (read only, pharmacist confirms)
- [ ] Analytics service and shortage early warning from Kafka events

## Run Locally

Start PostgreSQL, Kafka and Kafka UI:

```bash
docker compose up -d
```

Start each service from its folder:

```bash
cd services/request-service
mvn spring-boot:run
```

```bash
cd services/stock-service
./mvnw spring-boot:run
```

Start the storefront:

```bash
cd storefront-ui
npm install
npm run dev
```

| URL | What |
|---|---|
| http://localhost:5173 | Storefront |
| http://localhost:8090 | Kafka UI |
| http://localhost:8080/api/requests | Request Service API |
| http://localhost:8081/api/stock/pharmacies | Stock Service API |

## Tech Stack

**Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Kafka, PostgreSQL, Apache Kafka (KRaft), Maven, Lombok

**Frontend:** React 19, Vite, plain CSS

**Infrastructure:** Docker Compose, Kafka UI
