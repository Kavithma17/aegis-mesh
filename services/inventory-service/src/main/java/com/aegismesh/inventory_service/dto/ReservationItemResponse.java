package com.aegismesh.inventory_service.dto;


import java.util.UUID;

public record ReservationItemResponse(

        UUID id,
        String sku,
        Integer quantity

) {
}