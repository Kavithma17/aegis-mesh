package com.aegismesh.stock.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePharmacyRequest(

        @NotBlank(message = "Pharmacy code is required")
        String code,

        @NotBlank(message = "Pharmacy name is required")
        String name,

        @NotBlank(message = "Pharmacy location is required")
        String location

) {
}