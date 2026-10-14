package com.aegismesh.stock.dto;


import com.aegismesh.stock.entity.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(

        UUID id,
        String reservationNumber,
        UUID requestId,
        String pharmacyCode,
        ReservationStatus status,
        String rejectionReason,
        List<ReservationItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {
}