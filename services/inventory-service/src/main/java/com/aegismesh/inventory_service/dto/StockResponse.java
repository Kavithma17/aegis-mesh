package com.aegismesh.inventory_service.dto;

import java.util.UUID;

public record StockResponse(

        UUID id,

        String warehouseCode,

        String warehouseName,

        String sku,

        String productName,

        Integer onHandQuantity,

        Integer reservedQuantity,

        Integer availableQuantity

) {
}