# Aegis-Mesh

An event-driven microservice system that helps patients find scarce medicines across pharmacies and reserve them for pickup.

The project focuses on real distributed-system problems: each service owns its own database, services talk through Kafka, and stock is reserved safely when many people ask for the same limited medicine at the same time.

## Services

| Service | Port | Database | Responsibility |
|---|---|---|---|
| [Request Service](services/request-service) | 8080 | `request_db` (:5433) | Patients create, view, and cancel medicine requests. Publishes request events to Kafka. |
| [Stock Service](services/stock-service) | 8081 | `stock_db` (:5434) | Pharmacies, medicine stock, and concurrency-safe stock reservations. |

## Architecture

```text
Patient
   │
   ▼
Request Service ──► request_db
   │
   │  aegis.medicine-request.created
   ▼
Apache Kafka
   │
   ▼
Stock Service ──► stock_db
```

The Stock Service does not consume Kafka events yet. That is the next milestone.

## Concepts Covered

- Database per service
- REST APIs with validation
- Integration events instead of sharing JPA entities
- Kafka event publishing
- Reserved vs. available stock (`available = onHand - reserved`)
- Pessimistic row locking to prevent overselling
- Consistent lock ordering to reduce deadlocks
- Idempotent reservation release

## Roadmap

1. Stock Service consumes `medicine-request.created` and reserves stock automatically
2. Transactional outbox in the Request Service
3. Idempotent Kafka consumers
4. Pickup holds that expire and release stock
5. Retries and a dead-letter topic
6. AI Advisor that suggests other pharmacies or same-ingredient medicines (read only, pharmacist confirms)

## Run Locally

Start PostgreSQL and Kafka:

```bash
docker compose up -d
```

Then start each service from its folder:

```bash
cd services/request-service
mvn spring-boot:run
```

```bash
cd services/stock-service
./mvnw spring-boot:run
```

## Tech Stack

Java 21, Spring Boot, Spring Data JPA, Spring Kafka, PostgreSQL, Apache Kafka, Docker Compose, Maven, Lombok.
