package com.aegismesh.stock.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(

        @NotNull(message = "Request ID is required")
        UUID requestId,

        @NotBlank(message = "Pharmacy code is required")
        String pharmacyCode,

        @NotEmpty(message = "At least one item is required")
        List<@Valid ReservationItemRequest> items

) {
}