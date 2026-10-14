package com.aegismesh.request.controller;

import com.aegismesh.request.dto.CreateMedicineRequestDto;
import com.aegismesh.request.dto.MedicineRequestResponse;
import com.aegismesh.request.service.MedicineRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class MedicineRequestController {

    private final MedicineRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineRequestResponse createRequest(
            @Valid @RequestBody CreateMedicineRequestDto request
    ) {
        return requestService.createRequest(request);
    }

    @GetMapping
    public List<MedicineRequestResponse> getAllRequests() {
        return requestService.getAllRequests();
    }

    @GetMapping("/{requestId}")
    public MedicineRequestResponse getRequest(
            @PathVariable UUID requestId
    ) {
        return requestService.getRequest(requestId);
    }

    @GetMapping("/number/{requestNumber}")
    public MedicineRequestResponse getRequestByNumber(
            @PathVariable String requestNumber
    ) {
        return requestService.getRequestByNumber(requestNumber);
    }

    @PatchMapping("/{requestId}/cancel")
    public MedicineRequestResponse cancelRequest(
            @PathVariable UUID requestId
    ) {
        return requestService.cancelRequest(requestId);
    }
}