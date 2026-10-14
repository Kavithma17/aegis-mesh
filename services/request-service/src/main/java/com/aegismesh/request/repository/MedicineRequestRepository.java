package com.aegismesh.request.repository;

import com.aegismesh.request.entity.MedicineRequest;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;
import java.util.UUID;

public interface MedicineRequestRepository extends JpaRepository<MedicineRequest, UUID> {
//JpaRepository<MedicineRequest, UUID> here we can get save() findById() findAll() delete() count()

    Optional<MedicineRequest> findByRequestNumber(String requestNumber);

    boolean existsByRequestNumber(String requestNumber);
}