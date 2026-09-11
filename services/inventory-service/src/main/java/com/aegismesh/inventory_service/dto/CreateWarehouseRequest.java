package com.aegismesh.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseRequest(

        @NotBlank(message = "Warehouse code is required")
        String code,

        @NotBlank(message = "Warehouse name is required")
        String name,

        @NotBlank(message = "Warehouse location is required")
        String location

) {
}