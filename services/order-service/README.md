# AegisMesh Order Service

The first business microservice in the AegisMesh project.

## Responsibilities

- Create orders
- Retrieve one order
- Retrieve all orders
- Persist orders in PostgreSQL

## Endpoints

### Create order

`POST /api/orders`

```json
{
  "customerId": "CUS-001",
  "pickupAddress": "Colombo",
  "deliveryAddress": "Kandy",
  "packageDescription": "Laptop"
}
```

### Get one order

`GET /api/orders/{id}`

### Get all orders

`GET /api/orders`

## Run locally

Start PostgreSQL from the AegisMesh root Docker Compose file, then run:

```bash
mvn spring-boot:run
```

The service starts at:

`http://localhost:8081`

Health endpoint:

`GET /actuator/health`
