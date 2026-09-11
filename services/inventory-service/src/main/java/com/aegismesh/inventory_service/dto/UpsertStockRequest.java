package com.aegismesh.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

//Upsert  = Update if it exists OR Insert if it doesn't

public record UpsertStockRequest(

        @NotBlank(message = "Warehouse code is required")
        String warehouseCode,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "On-hand quantity is required")
        @PositiveOrZero(
                message = "On-hand quantity cannot be negative"
        )
        Integer onHandQuantity

) {
}