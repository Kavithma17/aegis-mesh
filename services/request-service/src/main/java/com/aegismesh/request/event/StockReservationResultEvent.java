package com.aegismesh.request.event;

import java.time.Instant;
import java.util.UUID;

/*
 * Saga step 3 -> 4.
 *
 * Published by the Stock Service after it tried to reserve
 * stock for a medicine request. This is our own copy of the
 * event: services share the JSON contract, not Java classes.
 *
 * status is RESERVED or REJECTED. It is a String so that a
 * new status added in the Stock Service does not break
 * deserialization here.
 *
 * reservationNumber is null when no reservation could be
 * created at all (for example, unknown pharmacy).
 */
public record StockReservationResultEvent(

        UUID eventId,

        UUID requestId,

        String reservationNumber,

        String status,

        String reason,

        Instant occurredAt

) {
}
