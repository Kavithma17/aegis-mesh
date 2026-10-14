package com.aegismesh.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

//Upsert  = Update if it exists OR Insert if it doesn't

public record UpsertStockRequest(

        @NotBlank(message = "Pharmacy code is required")
        String pharmacyCode,

        @NotBlank(message = "Medicine code is required")
        String medicineCode,

        @NotBlank(message = "Medicine name is required")
        String medicineName,

        @NotNull(message = "On-hand quantity is required")
        @PositiveOrZero(
                message = "On-hand quantity cannot be negative"
        )
        Integer onHandQuantity

) {
}