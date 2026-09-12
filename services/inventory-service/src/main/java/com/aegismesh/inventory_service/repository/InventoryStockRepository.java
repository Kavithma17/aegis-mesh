package com.aegismesh.inventory_service.repository;

import com.aegismesh.inventory_service.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select stock
        from InventoryStock stock
        join fetch stock.warehouse warehouse
        where warehouse.code = :warehouseCode
          and stock.sku = :sku
        """)
Optional<InventoryStock> findForUpdate(
        @Param("warehouseCode") String warehouseCode,
        @Param("sku") String sku
);
}