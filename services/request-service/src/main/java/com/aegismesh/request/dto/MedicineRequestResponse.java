package com.aegismesh.request.dto;

import com.aegismesh.request.entity.RequestStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MedicineRequestResponse(

        UUID id,
        String requestNumber,
        String patientId,
        String pharmacyCode,
        RequestStatus status,
        BigDecimal totalAmount,
        List<MedicineRequestItemResponse> items,
        Instant createdAt,
        Instant updatedAt

) {
}