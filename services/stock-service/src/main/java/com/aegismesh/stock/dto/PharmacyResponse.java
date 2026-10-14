package com.aegismesh.stock.dto;

import java.util.UUID;

public record PharmacyResponse(

        UUID id,
        String code,
        String name,
        String location,
        boolean active

) {
}