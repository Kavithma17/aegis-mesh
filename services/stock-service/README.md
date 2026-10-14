# Aegis-Mesh Inventory Service

The **Inventory Service** manages warehouses, product stock, and inventory reservations inside the Aegis-Mesh platform.

Its main responsibility is to answer:

> **Do we have enough stock for an order, and can we safely reserve it?**

The service owns its own PostgreSQL database and does not directly access data owned by other microservices.

---

## Responsibilities

The Inventory Service currently handles:

- Warehouse management
- Stock management per warehouse
- Product availability calculation
- Inventory reservations
- Reservation rejection when stock is unavailable
- Reservation release
- Concurrency-safe stock reservation
- Tracking reservation failure reasons

The service does **not** currently handle order creation, payments, shipping, customer notifications, Saga orchestration, or AI recovery.

Those responsibilities belong to other Aegis-Mesh services.

---

## Architecture

```text
                Inventory Service
                     :8081
                       │
                       ▼
                  PostgreSQL
                  inventory_db
                     :5434
```

The Inventory Service owns all inventory-related information.

```text
Inventory Service
│
├── Warehouse
├── InventoryStock
├── InventoryReservation
└── ReservationItem
```

---

## Warehouse

A warehouse represents a physical location where products are stored.

Example:

```text
Code:     CMB-01
Name:     Colombo Central Warehouse
Location: Colombo
Active:   true
```

The same product can exist in multiple warehouses.

```text
SKU: LAP-M4-16-256

CMB-01 → 10 units
KDY-01 → 4 units
GAL-01 → 7 units
```

This allows Aegis-Mesh to eventually reroute fulfillment when one warehouse cannot satisfy an order.

---

## Inventory Stock

`InventoryStock` represents the quantity of a particular product stored at a particular warehouse.

Example:

```text
Warehouse: CMB-01
SKU:       LAP-M4-16-256

onHandQuantity    = 10
reservedQuantity  = 3
availableQuantity = 7
```

Available inventory is calculated as:

```text
availableQuantity =
onHandQuantity - reservedQuantity
```

### Quantity Meaning

| Field | Meaning |
|---|---|
| `onHandQuantity` | Physical quantity stored at the warehouse |
| `reservedQuantity` | Quantity temporarily promised to active orders |
| `availableQuantity` | Quantity that can still be reserved |

`availableQuantity` is calculated rather than stored separately.

---

## Inventory Reservations

When an order requires products, the Inventory Service attempts to reserve the required stock.

```text
Order requests

Laptop × 1
Mouse  × 2

      ↓

Inventory Service

      ↓

Check warehouse stock

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
InventoryReservation
│
├── reservationNumber
├── orderId
├── warehouse
├── status
├── rejectionReason
├── createdAt
├── updatedAt
│
└── items
    ├── sku
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

The requested inventory was successfully reserved.

### `REJECTED`

The reservation could not be completed.

Example:

```text
Requested = 20
Available = 4
```

The reservation can store a failure reason such as:

```text
Insufficient stock for SKU LAP-M4-16-256 at warehouse CMB-01.
Requested=20, Available=4
```

### `RELEASED`

Previously reserved inventory was returned to the available inventory pool.

---

## Reservation Example

Suppose the warehouse contains:

```text
Laptop

onHand   = 10
reserved = 2
available = 8
```

An order requests:

```text
Laptop × 3
```

The Inventory Service checks:

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

`onHandQuantity` remains unchanged because the products have not physically left the warehouse yet.

They have only been reserved for the order.

---

## Releasing a Reservation

Reserved stock can be released if the order cannot continue.

For example:

```text
Inventory reserved
       ↓
Payment fails
       ↓
Release reservation
       ↓
Inventory becomes available again
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

This operation will later become part of the Aegis-Mesh **Saga compensation flow**.

---

## Concurrency Protection

Inventory reservation must safely handle multiple requests arriving at the same time.

Imagine that only one laptop is available:

```text
Available laptops = 1
```

Two orders arrive simultaneously.

Without concurrency protection:

```text
Order A reads available = 1
Order B reads available = 1

Order A reserves 1
Order B reserves 1
```

That would create two reservations when only one laptop exists.

This problem is known as **overselling**.

The Inventory Service prevents this using a pessimistic database lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

Conceptually:

```text
Order A
   ↓
Lock inventory row
   ↓
Check availability
   ↓
Reserve
   ↓
Commit
   ↓
Unlock

Order B
   ↓
Wait for lock
   ↓
Read updated availability
   ↓
Reserve or reject
```

This ensures two concurrent requests cannot reserve the same inventory incorrectly.

---

## Lock Ordering

Reservation items are sorted by SKU before database rows are locked.

Example:

```text
LAP-M4-16-256
MOUSE-WL-001
```

This helps transactions acquire locks in a consistent order and reduces the risk of database deadlocks.

Without consistent lock ordering:

```text
Transaction A

locks Laptop
waits for Mouse


Transaction B

locks Mouse
waits for Laptop
```

Both transactions could end up waiting for each other.

---

## Microservice Boundary

The Inventory Service does not import or persist the Order Service's `Order` entity.

Instead, a reservation stores only the order identifier:

```java
private UUID orderId;
```

The relationship between the two services is therefore logical rather than a JPA relationship.

```text
Order Service
     │
     │ orderId
     ▼
Inventory Service
```

Each service owns its own data.

```text
Order Service
     ↓
order_db


Inventory Service
     ↓
inventory_db
```

This keeps the microservices independently deployable and prevents database-level coupling.

---

# API Endpoints

## Create Warehouse

```http
POST /api/inventory/warehouses
```

Example request:

```json
{
  "code": "CMB-01",
  "name": "Colombo Central Warehouse",
  "location": "Colombo"
}
```

---

## Get Warehouses

```http
GET /api/inventory/warehouses
```

---

## Add or Update Stock

```http
PUT /api/inventory/stocks
```

Example request:

```json
{
  "warehouseCode": "CMB-01",
  "sku": "LAP-M4-16-256",
  "productName": "Laptop M4 16GB 256GB",
  "onHandQuantity": 10
}
```

---

## Find Stock by SKU

```http
GET /api/inventory/stocks/{sku}
```

Example:

```http
GET /api/inventory/stocks/LAP-M4-16-256
```

This endpoint can return availability across multiple warehouses.

Example:

```text
CMB-01 → 10 available
KDY-01 → 4 available
GAL-01 → 7 available
```

---

## Get Warehouse Stock

```http
GET /api/inventory/warehouses/{warehouseCode}/stocks
```

Example:

```http
GET /api/inventory/warehouses/CMB-01/stocks
```

---

## Create Reservation

```http
POST /api/inventory/reservations
```

Example request:

```json
{
  "orderId": "PUT-ORDER-UUID-HERE",
  "warehouseCode": "CMB-01",
  "items": [
    {
      "sku": "LAP-M4-16-256",
      "quantity": 1
    },
    {
      "sku": "MOUSE-WL-001",
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

---

## Get Reservation

```http
GET /api/inventory/reservations/{reservationNumber}
```

Example:

```http
GET /api/inventory/reservations/RES-A82D19FA
```

---

## Release Reservation

```http
PATCH /api/inventory/reservations/{reservationId}/release
```

This returns previously reserved inventory to the available inventory pool.

Example state transition:

```text
RESERVED
    ↓
RELEASED
```

---

# Local Configuration

The Inventory Service runs on:

```text
http://localhost:8081
```

PostgreSQL is exposed locally on:

```text
localhost:5434
```

Database:

```text
inventory_db
```

Example `application.properties`:

```properties
spring.application.name=inventory-service

server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5434/inventory_db
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

Move into the Inventory Service:

```bash
cd services/inventory-service
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
| PostgreSQL | Inventory database |
| Docker Compose | Local infrastructure |
| Maven | Build and dependency management |
| Lombok | Boilerplate reduction |

---

# Current Workflow

At the moment, the Order Service and Inventory Service operate independently.

The current flow is manual:

```text
Create Order
     ↓
Copy Order ID
     ↓
Call Inventory Reservation API
     ↓
Reserve or reject inventory
```

The Inventory Service does not yet automatically receive orders.

---

# Next Milestone: Apache Kafka

The next milestone will introduce event-driven communication between the Order Service and Inventory Service.

The flow will become:

```text
Customer creates order
        ↓
Order Service
        ↓
OrderCreated
        ↓
Apache Kafka
        ↓
Inventory Service
        ↓
Reserve inventory automatically
        ↓
   ┌────┴────┐
   │         │
   ▼         ▼
Inventory   Inventory
Reserved    Rejected
```

This removes the need to manually copy an order ID and call the Inventory Service.

---

# Future Role in Aegis-Mesh

The planned distributed workflow is:

```text
Order Service
      ↓
Kafka
      ↓
Saga Orchestrator
      ↓
Inventory Service
      ↓
Payment Service
      ↓
Fulfillment Service
```

When inventory cannot be fulfilled normally:

```text
Inventory Rejected
       ↓
Saga detects failure
       ↓
Recovery required
       ↓
AI Recovery Agent
       ↓
Check alternative warehouses
       ↓
Evaluate safe recovery options
       ↓
Retry fulfillment or compensate
```

The Inventory Service remains the **source of truth for stock and reservations**.

The future AI recovery layer will not modify inventory directly. It will only propose or request safe recovery actions through controlled service interfaces.
