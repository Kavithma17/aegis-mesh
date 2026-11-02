package com.aegismesh.stock.event;

import java.time.Instant;
import java.util.UUID;

/*
 * Saga compensation.
 *
 * Published by the Request Service when a patient cancels
 * a request. We release any stock held for it.
 */
public record MedicineRequestCancelledEvent(

        UUID eventId,

        UUID requestId,

        String requestNumber,

        Instant occurredAt

) {
}
