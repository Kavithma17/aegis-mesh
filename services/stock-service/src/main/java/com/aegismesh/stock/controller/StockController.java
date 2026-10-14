package com.aegismesh.stock.controller;

import com.aegismesh.stock.dto.*;
import com.aegismesh.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/pharmacies")
    @ResponseStatus(HttpStatus.CREATED)
    public PharmacyResponse createPharmacy(
            @Valid
            @RequestBody
            CreatePharmacyRequest request
    ) {
        return stockService
                .createPharmacy(request);
    }

    @GetMapping("/pharmacies")
    public List<PharmacyResponse> getPharmacies() {
        return stockService.getPharmacies();
    }

    @PutMapping("/medicines")
    public StockResponse upsertStock(
            @Valid
            @RequestBody
            UpsertStockRequest request
    ) {
        return stockService
                .upsertStock(request);
    }

    @GetMapping("/medicines/{medicineCode}")
    public List<StockResponse> getStockByMedicine(
            @PathVariable String medicineCode
    ) {
        return stockService
                .getStockByMedicine(medicineCode);
    }

    @GetMapping("/pharmacies/{pharmacyCode}/medicines")
    public List<StockResponse> getPharmacyStock(
            @PathVariable String pharmacyCode
    ) {
        return stockService
                .getPharmacyStock(pharmacyCode);
    }
}