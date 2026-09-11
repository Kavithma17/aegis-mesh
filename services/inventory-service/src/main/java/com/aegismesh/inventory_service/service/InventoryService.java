package com.aegismesh.inventory_service.service;

import com.aegismesh.inventory_service.dto.*;
import com.aegismesh.inventory_service.entity.InventoryStock;
import com.aegismesh.inventory_service.entity.Warehouse;
import com.aegismesh.inventory_service.repository.InventoryStockRepository;
import com.aegismesh.inventory_service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;

    @Transactional
    public WarehouseResponse createWarehouse(
            CreateWarehouseRequest request
    ) {

        if (warehouseRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Warehouse already exists: "
                            + request.code()
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.code())
                .name(request.name())
                .location(request.location())
                .active(true)
                .build();

        Warehouse savedWarehouse =
                warehouseRepository.save(warehouse);

        return toWarehouseResponse(savedWarehouse);
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getWarehouses() {

        return warehouseRepository.findAll()
                .stream()
                .map(this::toWarehouseResponse)
                .toList();
    }

    @Transactional
    public StockResponse upsertStock(
            UpsertStockRequest request
    ) {

        Warehouse warehouse =
                warehouseRepository
                        .findByCode(request.warehouseCode())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Warehouse not found: "
                                                + request.warehouseCode()
                                )
                        );

        InventoryStock stock =
                stockRepository
                        .findByWarehouse_CodeAndSku(
                                request.warehouseCode(),
                                request.sku()
                        )
                        .orElseGet(() ->
                                InventoryStock.builder()
                                        .warehouse(warehouse)
                                        .sku(request.sku())
                                        .reservedQuantity(0)
                                        .build()
                        );

        if (request.onHandQuantity()
                < stock.getReservedQuantity()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "On-hand quantity cannot be lower "
                            + "than reserved quantity"
            );
        }

        stock.setProductName(
                request.productName()
        );

        stock.setOnHandQuantity(
                request.onHandQuantity()
        );

        InventoryStock savedStock =
                stockRepository.save(stock);

        return toStockResponse(savedStock);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockBySku(
            String sku
    ) {

        return stockRepository
                .findBySkuOrderByWarehouse_Code(sku)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getWarehouseStock(
            String warehouseCode
    ) {

        if (!warehouseRepository.existsByCode(
                warehouseCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Warehouse not found: "
                            + warehouseCode
            );
        }

        return stockRepository
                .findByWarehouse_Code(warehouseCode)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }

    private WarehouseResponse toWarehouseResponse(
            Warehouse warehouse
    ) {

        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.isActive()
        );
    }

    private StockResponse toStockResponse(
            InventoryStock stock
    ) {

        return new StockResponse(
                stock.getId(),
                stock.getWarehouse().getCode(),
                stock.getWarehouse().getName(),
                stock.getSku(),
                stock.getProductName(),
                stock.getOnHandQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity()
        );
    }
}