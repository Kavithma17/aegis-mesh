package com.aegismesh.order.dto;

import com.aegismesh.order.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        String pickupAddress,
        String deliveryAddress,
        String packageDescription,
        OrderStatus status,
        Instant createdAt
) {
}
