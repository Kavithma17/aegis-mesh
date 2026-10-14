package com.aegismesh.order.event;

public record OrderItemEvent(
        String sku,
        Integer quantity
) {
}