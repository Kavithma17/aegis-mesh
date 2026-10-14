package com.aegismesh.stock.dto;

import java.util.UUID;

public record StockResponse(

        UUID id,

        String pharmacyCode,

        String pharmacyName,

        String medicineCode,

        String medicineName,

        Integer onHandQuantity,

        Integer reservedQuantity,

        Integer availableQuantity

) {
}