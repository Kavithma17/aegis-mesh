# AegisMesh Medicine Request Service

The service where patients request medicines for pickup at a pharmacy.

## Responsibilities

- Create medicine requests
- Retrieve one request (by ID or request number)
- Retrieve all requests
- Cancel a request
- Persist requests in PostgreSQL (`request_db`)
- Publish a `MedicineRequestCreatedEvent` to Kafka when a request is created

## Endpoints

### Create request

`POST /api/requests`

```json
{
  "patientId": "PAT-001",
  "pharmacyCode": "CMB-01",
  "items": [
    {
      "medicineCode": "PARA-500",
      "medicineName": "Paracetamol 500mg",
      "quantity": 20,
      "unitPrice": 5.00
    }
  ]
}
```

The response contains a generated request number such as `MED-1A2B3C4D` and the status `CREATED`.

### Get one request

`GET /api/requests/{id}`

### Get a request by number

`GET /api/requests/number/{requestNumber}`

### Get all requests

`GET /api/requests`

### Cancel a request

`PATCH /api/requests/{id}/cancel`

A request whose stock is already reserved (`STOCK_RESERVED`) cannot be cancelled yet.

## Request Status

```text
CREATED
STOCK_PENDING
STOCK_RESERVED
STOCK_REJECTED
CANCELLED
```

## Events

When a request is created, the service publishes to the Kafka topic:

`aegis.medicine-request.created`

The message key is the request ID. The event contains the request ID, request number, patient ID, pharmacy code, and the requested medicines (`medicineCode`, `quantity`).

## Run locally

Start PostgreSQL and Kafka from the AegisMesh root Docker Compose file, then run:

```bash
mvn spring-boot:run
```

The service starts at:

`http://localhost:8080`

Health endpoint:

`GET /actuator/health`
