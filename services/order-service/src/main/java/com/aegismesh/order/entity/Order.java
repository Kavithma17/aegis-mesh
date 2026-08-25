package com.aegismesh.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String packageDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = OrderStatus.CREATED;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
