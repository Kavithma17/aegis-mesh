package com.aegismesh.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "medicine_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_pharmacy_medicine",
                        columnNames = {
                                "pharmacy_id",
                                "medicine_code"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pharmacy_id",
            nullable = false
    )
    private Pharmacy pharmacy;

    @Column(nullable = false, length = 100)
    private String medicineCode;

    @Column(nullable = false)
    private String medicineName;

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
    public boolean canReserve(int quantity) {
    return getAvailableQuantity() >= quantity;
}

public void reserve(int quantity) {

    if (!canReserve(quantity)) {
        throw new IllegalStateException(
                "Insufficient available stock"
        );
    }

    reservedQuantity += quantity;
}

public void release(int quantity) {

    if (reservedQuantity < quantity) {
        throw new IllegalStateException(
                "Cannot release more stock than reserved"
        );
    }

    reservedQuantity -= quantity;
}


}