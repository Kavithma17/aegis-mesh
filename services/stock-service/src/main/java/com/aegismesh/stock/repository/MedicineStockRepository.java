package com.aegismesh.stock.repository;

import com.aegismesh.stock.entity.MedicineStock;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineStockRepository
        extends JpaRepository<MedicineStock, UUID> {

    Optional<MedicineStock> findByPharmacy_CodeAndMedicineCode(
            String pharmacyCode,
            String medicineCode
    );

    List<MedicineStock> findByMedicineCodeOrderByPharmacy_Code(
            String medicineCode
    );

    List<MedicineStock> findByPharmacy_Code(
            String pharmacyCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select stock
        from MedicineStock stock
        join fetch stock.pharmacy pharmacy
        where pharmacy.code = :pharmacyCode
          and stock.medicineCode = :medicineCode
        """)
Optional<MedicineStock> findForUpdate(
        @Param("pharmacyCode") String pharmacyCode,
        @Param("medicineCode") String medicineCode
);
}