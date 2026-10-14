package com.aegismesh.request.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MedicineRequestItemResponse(

        UUID id,
        String medicineCode,
        String medicineName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal

) {
}
