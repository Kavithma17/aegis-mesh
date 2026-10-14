package com.aegismesh.request.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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