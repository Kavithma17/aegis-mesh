package com.aegismesh.stock.dto;


import java.util.UUID;

public record ReservationItemResponse(

        UUID id,
        String medicineCode,
        Integer quantity

) {
}