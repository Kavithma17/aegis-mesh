package com.aegismesh.request.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateMedicineRequestDto(

        @NotBlank(message = "Patient ID is required")
        String patientId,

        @NotBlank(message = "Pharmacy code is required")
        String pharmacyCode,

        @NotEmpty(message = "Request must contain at least one medicine")
        List<@Valid MedicineRequestItemDto> items

) {
}