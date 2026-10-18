package com.aegismesh.stock.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
 * Stock Service's own copy of the event published by the
 * Request Service. We do not share Java classes between
 * services; only the JSON shape must match.
 */
public record MedicineRequestCreatedEvent(

        UUID eventId,

        UUID requestId,

        String requestNumber,

        String patientId,

        String pharmacyCode,

        List<RequestItemEvent> items,

        Instant occurredAt

) {
}
