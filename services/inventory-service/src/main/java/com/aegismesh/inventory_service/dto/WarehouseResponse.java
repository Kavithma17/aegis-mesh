package com.aegismesh.inventory_service.dto;

import java.util.UUID;

public record WarehouseResponse(

        UUID id,
        String code,
        String name,
        String location,
        boolean active

) {
}