# GitHub Issues (draft, copy into GitHub)

Suggested labels: `phase-1` … `phase-6`, `stock-service`, `request-service`, `kafka`, `testing`, `devops`, `frontend`, `docs`, `enhancement`
Suggested milestones: one per phase (e.g. "Phase 1: Reservation Saga").

Delete this file once the issues are created.

---

## 1. Stock Service consumes `medicine-request.created` and reserves stock

**Labels:** `phase-1`, `stock-service`, `kafka` · **Milestone:** Phase 1: Reservation Saga

When a patient creates a request, the Stock Service should reserve stock automatically instead of waiting for a REST call.

**Tasks**
- [ ] Add Spring Kafka consumer config to stock-service
- [ ] Add the Stock Service's own copy of `MedicineRequestCreatedEvent` (no shared classes)
- [ ] `@KafkaListener` maps the event to `createReservation`

**Acceptance criteria**
- Creating a request via `POST /api/requests` produces a reservation in `stock_db`
- Reservation is `RESERVED` when stock is available, `REJECTED` otherwise

---

## 2. Idempotent reservation consumer

**Labels:** `phase-1`, `stock-service`, `kafka` · **Milestone:** Phase 1: Reservation Saga

Kafka delivers at least once. A redelivered event must not reserve stock twice.

**Tasks**
- [ ] Unique constraint on `stock_reservations.request_id`
- [ ] `createReservation` returns the existing reservation if one exists for the request
- [ ] Concurrent duplicates are caught by the unique constraint and retried

**Acceptance criteria**
- Publishing the same event twice results in exactly one reservation and one stock decrement

---

## 3. Publish `aegis.stock.reservation-result` event

**Labels:** `phase-1`, `stock-service`, `kafka` · **Milestone:** Phase 1: Reservation Saga

The Request Service needs to know the outcome of the reservation.

**Tasks**
- [ ] `StockReservationResultEvent` (eventId, requestId, reservationNumber, status, reason, occurredAt)
- [ ] Publish after the reservation transaction commits
- [ ] Business failures (unknown/inactive pharmacy) publish `REJECTED` instead of retrying

**Acceptance criteria**
- Every processed request event produces exactly one result outcome (redeliveries are safe)

---

## 4. Request Service consumes reservation result

**Labels:** `phase-1`, `request-service`, `kafka` · **Milestone:** Phase 1: Reservation Saga

**Tasks**
- [ ] Consumer for `aegis.stock.reservation-result`
- [ ] Update status to `STOCK_RESERVED` or `STOCK_REJECTED`
- [ ] Store `reservationNumber` and rejection reason on the request and expose them in the API
- [ ] Ignore results for requests already in a final state (idempotent)

**Acceptance criteria**
- `GET /api/requests/{id}` shows the final status a few seconds after creation

---

## 5. Retries and dead-letter topic

**Labels:** `phase-1`, `kafka` · **Milestone:** Phase 1: Reservation Saga

**Tasks**
- [ ] `DefaultErrorHandler` with fixed/exponential backoff
- [ ] `DeadLetterPublishingRecoverer` sends failed records to `<topic>.DLT`
- [ ] Deserialization errors go straight to the DLT (no retry)

**Acceptance criteria**
- A malformed message ends up in the DLT and does not block the partition

---

## 6. Release stock when a request is cancelled

**Labels:** `phase-2`, `request-service`, `stock-service`, `kafka` · **Milestone:** Phase 2: Compensation

**Tasks**
- [ ] Allow cancelling `STOCK_RESERVED` requests
- [ ] Publish `aegis.medicine-request.cancelled`
- [ ] Stock Service releases the reservation for that request (idempotent)
- [ ] Handle the race: request cancelled before the reservation result arrives

**Acceptance criteria**
- After cancel, `reservedQuantity` returns to its previous value

---

## 7. Expire pickup holds

**Labels:** `phase-2`, `stock-service` · **Milestone:** Phase 2: Compensation

**Tasks**
- [ ] `expiresAt` on reservations (configurable, e.g. 24h)
- [ ] Scheduled job releases expired reservations and publishes `aegis.stock.reservation-expired`
- [ ] Request Service marks the request `EXPIRED`
- [ ] Safe to run on multiple instances (`SELECT ... FOR UPDATE SKIP LOCKED` or ShedLock)

---

## 8. Transactional outbox in Request Service

**Labels:** `phase-3`, `request-service`, `kafka` · **Milestone:** Phase 3: Reliability

Today the request is saved and the event is sent separately. If Kafka is down the event is lost; if the DB rolls back a phantom event is sent.

**Tasks**
- [ ] `outbox_events` table written in the same transaction as the request
- [ ] Relay publishes pending rows to Kafka and marks them sent
- [ ] Document the trade-offs (polling vs. Debezium CDC)

**Acceptance criteria**
- Stopping Kafka, creating requests, then starting Kafka delivers every event

---

## 9. Integration tests with Testcontainers

**Labels:** `phase-4`, `testing` · **Milestone:** Phase 4: Testing

- [ ] PostgreSQL + Kafka containers via `@ServiceConnection`
- [ ] Repository and controller tests for both services

---

## 10. Concurrency test: no overselling

**Labels:** `phase-4`, `testing`, `stock-service` · **Milestone:** Phase 4: Testing

- [ ] Stock = 10, fire 50 parallel reservations of 1
- [ ] Assert exactly 10 `RESERVED`, 40 `REJECTED`, `reservedQuantity == 10`

---

## 11. End-to-end saga test

**Labels:** `phase-4`, `testing` · **Milestone:** Phase 4: Testing

- [ ] Create request → assert `STOCK_RESERVED` (Awaitility)
- [ ] Insufficient stock → assert `STOCK_REJECTED`
- [ ] Cancel → assert stock released

---

## 12. Dockerize services

**Labels:** `phase-5`, `devops` · **Milestone:** Phase 5: DevOps & Observability

- [ ] Multi-stage Dockerfile per service
- [ ] `docker compose up` starts databases, Kafka and both services
- [ ] Config through environment variables

---

## 13. GitHub Actions CI

**Labels:** `phase-5`, `devops` · **Milestone:** Phase 5: DevOps & Observability

- [ ] Build and test both services on every PR to `develop`
- [ ] Build status badge in README

---

## 14. Distributed tracing

**Labels:** `phase-5`, `devops` · **Milestone:** Phase 5: DevOps & Observability

- [ ] Micrometer Tracing + OpenTelemetry in both services
- [ ] Trace context propagated through Kafka headers
- [ ] Jaeger/Zipkin in docker-compose; screenshot of one request across both services

---

## 15. Align Spring Boot versions

**Labels:** `phase-5`, `devops` · **Milestone:** Phase 5: DevOps & Observability

request-service is on Spring Boot 3.5.5, stock-service on 4.1.1.

- [ ] Upgrade request-service to the same version
- [ ] Use the Maven wrapper in both services

---

## 16. Connect UIs to the APIs

**Labels:** `phase-6`, `frontend` · **Milestone:** Phase 6: Product

- [ ] Storefront: search medicine availability, create request, see status
- [ ] Admin dashboard: manage pharmacies and stock, view reservations

---

## 17. AI Advisor

**Labels:** `phase-6`, `enhancement` · **Milestone:** Phase 6: Product

When a request is rejected, suggest alternatives.

- [ ] Suggest nearby pharmacies with available stock
- [ ] Suggest same-active-ingredient medicines
- [ ] Read only: a pharmacist must confirm any substitution

---

## 18. Architecture docs and demo

**Labels:** `phase-6`, `docs` · **Milestone:** Phase 6: Product

- [ ] Architecture and sequence diagrams for the saga
- [ ] Demo GIF in the README
- [ ] Short write-up of design decisions (locking, idempotency, outbox)
