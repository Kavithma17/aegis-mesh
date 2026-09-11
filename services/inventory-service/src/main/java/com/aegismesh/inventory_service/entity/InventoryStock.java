package com.aegismesh.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "inventory_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_warehouse_sku",
                        columnNames = {
                                "warehouse_id",
                                "sku"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "warehouse_id",
            nullable = false
    )
    private Warehouse warehouse;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer onHandQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    private Long version;

    public int getAvailableQuantity() {
        return onHandQuantity - reservedQuantity;
    }
}