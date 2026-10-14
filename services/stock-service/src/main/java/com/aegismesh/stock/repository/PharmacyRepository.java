package com.aegismesh.stock.repository;

import com.aegismesh.stock.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PharmacyRepository
        extends JpaRepository<Pharmacy, UUID> {

    Optional<Pharmacy> findByCode(String code);

    boolean existsByCode(String code);
}