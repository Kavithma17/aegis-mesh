package com.aegismesh.stock.event;

import com.aegismesh.stock.entity.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

/*
 * Published after the Stock Service has tried to reserve
 * stock for a medicine request.
 *
 * reservationNumber is null when no reservation could be
 * created at all (for example, unknown pharmacy).
 */
public record StockReservationResultEvent(

        UUID eventId,

        UUID requestId,

        String reservationNumber,

        ReservationStatus status,

        String reason,

        Instant occurredAt

) {
}
