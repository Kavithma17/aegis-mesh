package com.aegismesh.inventory_service.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(

        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotBlank(message = "Warehouse code is required")
        String warehouseCode,

        @NotEmpty(message = "At least one item is required")
        List<@Valid ReservationItemRequest> items

) {
}