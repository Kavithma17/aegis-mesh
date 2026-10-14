package com.aegismesh.order.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(

        UUID eventId,

        UUID orderId,

        String orderNumber,

        String customerId,

        String deliveryAddress,

        List<OrderItemEvent> items,

        Instant occurredAt

) {
}