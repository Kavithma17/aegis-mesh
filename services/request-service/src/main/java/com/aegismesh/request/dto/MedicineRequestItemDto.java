package com.aegismesh.request.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MedicineRequestItemDto(

        @NotBlank(message = "Medicine code is required")
        String medicineCode,

        @NotBlank(message = "Medicine name is required")
        String medicineName,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @NotNull(message = "Unit price is required")
        @DecimalMin(
                value = "0.01",
                message = "Unit price must be greater than zero"
        )
        BigDecimal unitPrice

) {
}