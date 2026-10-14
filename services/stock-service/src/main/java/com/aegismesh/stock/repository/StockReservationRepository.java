package com.aegismesh.stock.repository;


import com.aegismesh.stock.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository
        extends JpaRepository<StockReservation, UUID> {

    Optional<StockReservation> findByReservationNumber(
            String reservationNumber
    );
}