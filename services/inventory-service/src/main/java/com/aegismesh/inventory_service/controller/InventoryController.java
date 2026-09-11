package com.aegismesh.inventory_service.controller;

import com.aegismesh.inventory_service.dto.*;
import com.aegismesh.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/warehouses")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse createWarehouse(
            @Valid
            @RequestBody
            CreateWarehouseRequest request
    ) {
        return inventoryService
                .createWarehouse(request);
    }

    @GetMapping("/warehouses")
    public List<WarehouseResponse> getWarehouses() {
        return inventoryService.getWarehouses();
    }

    @PutMapping("/stocks")
    public StockResponse upsertStock(
            @Valid
            @RequestBody
            UpsertStockRequest request
    ) {
        return inventoryService
                .upsertStock(request);
    }

    @GetMapping("/stocks/{sku}")
    public List<StockResponse> getStockBySku(
            @PathVariable String sku
    ) {
        return inventoryService
                .getStockBySku(sku);
    }

    @GetMapping("/warehouses/{warehouseCode}/stocks")
    public List<StockResponse> getWarehouseStock(
            @PathVariable String warehouseCode
    ) {
        return inventoryService
                .getWarehouseStock(warehouseCode);
    }
}