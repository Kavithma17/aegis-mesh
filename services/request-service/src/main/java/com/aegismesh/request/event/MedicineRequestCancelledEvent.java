package com.aegismesh.request.event;

import java.time.Instant;
import java.util.UUID;

/*
 * Saga compensation.
 *
 * Published when a patient cancels a request. The Stock
 * Service releases any stock it reserved for this request.
 */
public record MedicineRequestCancelledEvent(

        UUID eventId,

        UUID requestId,

        String requestNumber,

        Instant occurredAt

) {
}
