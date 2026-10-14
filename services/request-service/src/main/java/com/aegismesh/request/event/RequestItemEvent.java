package com.aegismesh.request.event;

public record RequestItemEvent(
        String medicineCode,
        Integer quantity
) {
}