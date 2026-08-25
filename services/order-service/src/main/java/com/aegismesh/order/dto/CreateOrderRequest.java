package com.aegismesh.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "pickupAddress is required")
        String pickupAddress,

        @NotBlank(message = "deliveryAddress is required")
        String deliveryAddress,

        @NotBlank(message = "packageDescription is required")
        @Size(max = 500, message = "packageDescription must be at most 500 characters")
        String packageDescription
) {
}
