package com.aegismesh.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(

        UUID id,
        String sku,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal

) {
}
