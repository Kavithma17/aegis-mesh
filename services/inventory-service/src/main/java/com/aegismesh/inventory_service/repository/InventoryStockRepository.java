package com.aegismesh.inventory_service.repository;

import com.aegismesh.inventory_service.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, UUID> {

    Optional<InventoryStock> findByWarehouse_CodeAndSku(
            String warehouseCode,
            String sku
    );

    List<InventoryStock> findBySkuOrderByWarehouse_Code(
            String sku
    );

    List<InventoryStock> findByWarehouse_Code(
            String warehouseCode
    );
}