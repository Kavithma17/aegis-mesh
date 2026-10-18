package com.aegismesh.stock.event;

public record RequestItemEvent(
        String medicineCode,
        Integer quantity
) {
}
