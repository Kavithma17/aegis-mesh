package com.aegismesh.inventory_service.dto;


import com.aegismesh.inventory_service.entity.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(

        UUID id,
        String reservationNumber,
        UUID orderId,
        String warehouseCode,
        ReservationStatus status,
        String rejectionReason,
        List<ReservationItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {
}