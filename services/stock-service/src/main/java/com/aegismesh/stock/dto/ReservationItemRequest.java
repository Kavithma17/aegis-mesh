package com.aegismesh.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemRequest(

        @NotBlank(message = "Medicine code is required")
        String medicineCode,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity

) {
}