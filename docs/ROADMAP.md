# Roadmap

Aegis-Mesh is built in phases. Each phase adds one distributed-systems concept on top of the previous one.

Status: ✅ done · 🚧 in progress · ⏳ planned

## Phase 0: Foundations ✅

- ✅ Request Service: create, view and cancel medicine requests
- ✅ Stock Service: pharmacies, medicine stock, reservations
- ✅ Database per service (PostgreSQL)
- ✅ Pessimistic row locking with consistent lock ordering to prevent overselling
- ✅ Request Service publishes `aegis.medicine-request.created` to Kafka

## Phase 1: Reservation Saga 🚧

Close the loop between the two services so a patient request is reserved automatically.

```text
Request Service ──created──► Kafka ──► Stock Service
       ▲                                    │
       └──────── reservation-result ◄───────┘
```

- 🚧 Stock Service consumes `medicine-request.created` and reserves stock
- ⏳ Idempotent consumer: a redelivered event never creates a second reservation
- ⏳ Stock Service publishes `aegis.stock.reservation-result`
- ⏳ Request Service consumes the result and moves to `STOCK_RESERVED` / `STOCK_REJECTED`
- ⏳ Retries with backoff and a dead-letter topic for poison messages

## Phase 2: Compensation ⏳

- ⏳ Cancelling a request publishes `medicine-request.cancelled`; Stock Service releases the reservation
- ⏳ Pickup holds expire after a time limit and release stock automatically

## Phase 3: Reliability ⏳

- ⏳ Transactional outbox in the Request Service (no lost or phantom events)

## Phase 4: Testing ⏳

- ⏳ Integration tests with Testcontainers (PostgreSQL + Kafka)
- ⏳ Concurrency test: N parallel reservations never oversell stock
- ⏳ End-to-end saga test

## Phase 5: DevOps & Observability ⏳

- ⏳ Dockerfile per service, full stack in `docker-compose`
- ⏳ GitHub Actions CI on every pull request
- ⏳ Distributed tracing with Micrometer + OpenTelemetry
- ⏳ Align both services on the same Spring Boot version

## Phase 6: Product ⏳

- ⏳ Connect storefront and admin dashboard UIs to the APIs
- ⏳ AI Advisor: suggests other pharmacies or same-ingredient medicines (read only, pharmacist confirms)
- ⏳ Architecture diagram and demo GIF in the README
