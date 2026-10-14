# Aegis-Mesh Stock Service

The **Stock Service** manages pharmacies, medicine stock, and stock reservations inside the Aegis-Mesh platform.

Its main responsibility is to answer:

> **Does this pharmacy have enough of the requested medicines, and can we safely hold them for the patient?**

The service owns its own PostgreSQL database and does not directly access data owned by other microservices.

---

## Responsibilities

The Stock Service currently handles:

- Pharmacy management
- Medicine stock management per pharmacy
- Medicine availability calculation
- Stock reservations
- Reservation rejection when stock is unavailable
- Reservation release
- Concurrency-safe stock reservation
- Tracking reservation failure reasons

The service does **not** handle creating medicine requests, notifications, or AI suggestions.

Those responsibilities belong to other Aegis-Mesh services.

---

## Architecture

```text
                  Stock Service
                     :8081
                       │
                       ▼
                  PostgreSQL
                   stock_db
                     :5434
```

The Stock Service owns all stock-related information.

```text
Stock Service
│
├── Pharmacy
├── MedicineStock
├── StockReservation
└── ReservationItem
```

---

## Pharmacy

A pharmacy represents a physical location where medicines are stocked.

Example:

```text
Code:     CMB-01
Name:     Colombo City Pharmacy
Location: Colombo
Active:   true
```

The same medicine can exist in multiple pharmacies.

```text
Medicine code: PARA-500

CMB-01 → 10 units
KDY-01 → 4 units
GAL-01 → 7 units
```

This lets Aegis-Mesh show a patient other pharmacies when one pharmacy cannot satisfy a request.

---

## Medicine Stock

`MedicineStock` represents the quantity of a particular medicine stocked at a particular pharmacy.

Example:

```text
Pharmacy:      CMB-01
Medicine code: PARA-500

onHandQuantity    = 10
reservedQuantity  = 3
availableQuantity = 7
```

Available stock is calculated as:

```text
availableQuantity =
onHandQuantity - reservedQuantity
```

### Quantity Meaning

| Field | Meaning |
|---|---|
| `onHandQuantity` | Physical quantity on the pharmacy's shelves |
| `reservedQuantity` | Quantity temporarily held for active requests |
| `availableQuantity` | Quantity that can still be reserved |

`availableQuantity` is calculated rather than stored separately.

---

## Stock Reservations

When a patient requests medicines, the Stock Service attempts to reserve the required stock.

```text
Patient requests

Paracetamol × 1
Amoxicillin × 2

      ↓

Stock Service

      ↓

Check pharmacy stock

      ↓

Enough stock?

   ┌───────┴───────┐
   │               │
  YES              NO
   │               │
   ▼               ▼
RESERVED         REJECTED
```

A reservation contains:

```text
StockReservation
│
├── reservationNumber
├── requestId
├── pharmacy
├── status
├── rejectionReason
├── createdAt
├── updatedAt
│
└── items
    ├── medicineCode
    └── quantity
```

---

## Reservation Status

The current reservation states are:

```text
RESERVED
REJECTED
RELEASED
```

### `RESERVED`

The requested medicines were successfully reserved.

### `REJECTED`

The reservation could not be completed.

Example:

```text
Requested = 20
Available = 4
```

The reservation can store a failure reason such as:

```text
Insufficient stock for medicine PARA-500 at pharmacy CMB-01.
Requested=20, Available=4
```

### `RELEASED`

Previously reserved stock was returned to the available stock pool.

---

## Reservation Example

Suppose the pharmacy contains:

```text
Paracetamol

onHand   = 10
reserved = 2
available = 8
```

A patient requests:

```text
Paracetamol × 3
```

The Stock Service checks:

```text
requested = 3
available = 8

3 <= 8
```

The reservation succeeds.

After reservation:

```text
onHand   = 10
reserved = 5
available = 5
```

`onHandQuantity` remains unchanged because the medicines have not physically left the pharmacy yet.

They have only been reserved for the patient's request.

---

## Releasing a Reservation

Reserved stock can be released if the request cannot continue.

For example:

```text
Stock reserved
       ↓
Patient cancels or does not pick up
       ↓
Release reservation
       ↓
Stock becomes available again
```

Before release:

```text
onHand   = 10
reserved = 5
available = 5
```

After releasing three units:

```text
onHand   = 10
reserved = 2
available = 8
```

This operation will later be triggered automatically when a request is cancelled or a pickup hold expires.

---

## Concurrency Protection

Stock reservation must safely handle multiple requests arriving at the same time.

Imagine that only one pack of a medicine is available:

```text
Available packs = 1
```

Two requests arrive simultaneously.

Without concurrency protection:

```text
Request A reads available = 1
Request B reads available = 1

Request A reserves 1
Request B reserves 1
```

That would create two reservations when only one pack exists.

This problem is known as **overselling**.

The Stock Service prevents this using a pessimistic database lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

Conceptually:

```text
Request A
   ↓
Lock stock row
   ↓
Check availability
   ↓
Reserve
   ↓
Commit
   ↓
Unlock

Request B
   ↓
Wait for lock
   ↓
Read updated availability
   ↓
Reserve or reject
```

This ensures two concurrent requests cannot reserve the same stock incorrectly.

---

## Lock Ordering

Reservation items are sorted by medicine code before database rows are locked.

Example:

```text
AMOX-250
PARA-500
```

This helps transactions acquire locks in a consistent order and reduces the risk of database deadlocks.

Without consistent lock ordering:

```text
Transaction A

locks Paracetamol
waits for Amoxicillin


Transaction B

locks Amoxicillin
waits for Paracetamol
```

Both transactions could end up waiting for each other.

---

## Microservice Boundary

The Stock Service does not import or persist the Request Service's `MedicineRequest` entity.

Instead, a reservation stores only the request identifier:

```java
private UUID requestId;
```

The relationship between the two services is therefore logical rather than a JPA relationship.

```text
Request Service
     │
     │ requestId
     ▼
Stock Service
```

Each service owns its own data.

```text
Request Service
     ↓
request_db


Stock Service
     ↓
stock_db
```

This keeps the microservices independently deployable and prevents database-level coupling.

---

# API Endpoints

## Create Pharmacy

```http
POST /api/stock/pharmacies
```

Example request:

```json
{
  "code": "CMB-01",
  "name": "Colombo City Pharmacy",
  "location": "Colombo"
}
```

---

## Get Pharmacies

```http
GET /api/stock/pharmacies
```

---

## Add or Update Stock

```http
PUT /api/stock/medicines
```

Example request:

```json
{
  "pharmacyCode": "CMB-01",
  "medicineCode": "PARA-500",
  "medicineName": "Paracetamol 500mg",
  "onHandQuantity": 10
}
```

---

## Find Stock by Medicine

```http
GET /api/stock/medicines/{medicineCode}
```

Example:

```http
GET /api/stock/medicines/PARA-500
```

This endpoint can return availability across multiple pharmacies.

Example:

```text
CMB-01 → 10 available
KDY-01 → 4 available
GAL-01 → 7 available
```

---

## Get Pharmacy Stock

```http
GET /api/stock/pharmacies/{pharmacyCode}/medicines
```

Example:

```http
GET /api/stock/pharmacies/CMB-01/medicines
```

---

## Reservation Endpoints (not exposed yet)

> **Note:** The reservation logic exists in `StockService` (`createReservation`, `getReservation`, `releaseReservation`), but `StockController` does not expose these endpoints yet. They are planned as follows.

### Create Reservation

```http
POST /api/stock/reservations
```

Example request:

```json
{
  "requestId": "PUT-REQUEST-UUID-HERE",
  "pharmacyCode": "CMB-01",
  "items": [
    {
      "medicineCode": "PARA-500",
      "quantity": 1
    },
    {
      "medicineCode": "AMOX-250",
      "quantity": 2
    }
  ]
}
```

Successful reservation:

```text
status = RESERVED
```

Failed reservation:

```text
status = REJECTED
```

### Get Reservation

```http
GET /api/stock/reservations/{reservationNumber}
```

Example:

```http
GET /api/stock/reservations/RES-A82D19FA
```

### Release Reservation

```http
PATCH /api/stock/reservations/{reservationId}/release
```

This returns previously reserved stock to the available stock pool.

Example state transition:

```text
RESERVED
    ↓
RELEASED
```

---

# Local Configuration

The Stock Service runs on:

```text
http://localhost:8081
```

PostgreSQL is exposed locally on:

```text
localhost:5434
```

Database:

```text
stock_db
```

Example `application.properties`:

```properties
spring.application.name=stock-service

server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5434/stock_db
spring.datasource.username=aegismesh
spring.datasource.password=aegismesh

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

---

# Running the Service

Start the PostgreSQL databases from the Aegis-Mesh project root:

```bash
docker compose up -d
```

Check the running containers:

```bash
docker compose ps
```

Move into the Stock Service:

```bash
cd services/stock-service
```

Run the tests:

```bash
mvn clean test
```

Start the service:

```bash
mvn spring-boot:run
```

The application should start on:

```text
http://localhost:8081
```

---

## Health Check

```http
GET http://localhost:8081/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

# Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language |
| Spring Boot 4 | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Persistence layer |
| Bean Validation | API request validation |
| PostgreSQL | Stock database |
| Docker Compose | Local infrastructure |
| Maven | Build and dependency management |
| Lombok | Boilerplate reduction |

---

# Current Workflow

At the moment, the Request Service and Stock Service operate independently.

The Request Service already publishes `aegis.medicine-request.created` events to Kafka, but the Stock Service does not consume them yet.

The flow is therefore manual:

```text
Create medicine request
     ↓
Copy request ID
     ↓
Reserve stock in the Stock Service
     ↓
Reserve or reject
```

---

# Next Milestone: Kafka Consumer

The next milestone makes the Stock Service react to medicine request events automatically.

```text
Patient creates request
        ↓
Request Service
        ↓
aegis.medicine-request.created
        ↓
Apache Kafka
        ↓
Stock Service
        ↓
Reserve stock automatically
        ↓
   ┌────┴────┐
   │         │
   ▼         ▼
 Stock      Stock
Reserved   Rejected
```

This removes the need to copy a request ID manually.

---

# Future Role in Aegis-Mesh

The planned workflow is:

```text
Request Service
      ↓
Kafka
      ↓
Stock Service
      ↓
Stock reserved → pickup hold with an expiry time
Stock rejected → AI Advisor
```

When a request cannot be met:

```text
Stock Rejected
       ↓
AI Advisor
       ↓
Check other pharmacies (read only)
       ↓
Suggest medicines with the same active ingredient
       ↓
Pharmacist confirms
       ↓
Patient makes a new request
```

The Stock Service remains the **source of truth for stock and reservations**.

The AI Advisor will never modify stock directly. It only suggests options, and every change still goes through the normal request and reservation flow.
