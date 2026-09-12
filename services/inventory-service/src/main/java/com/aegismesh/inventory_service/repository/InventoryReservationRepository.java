package com.aegismesh.inventory_service.repository;


import com.aegismesh.inventory_service.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByReservationNumber(
            String reservationNumber
    );
}