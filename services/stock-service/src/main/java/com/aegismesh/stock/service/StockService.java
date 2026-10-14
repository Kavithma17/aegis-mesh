package com.aegismesh.stock.service;

import com.aegismesh.stock.dto.CreateReservationRequest;
import com.aegismesh.stock.dto.CreatePharmacyRequest;
import com.aegismesh.stock.dto.ReservationItemRequest;
import com.aegismesh.stock.dto.ReservationItemResponse;
import com.aegismesh.stock.dto.ReservationResponse;
import com.aegismesh.stock.dto.StockResponse;
import com.aegismesh.stock.dto.UpsertStockRequest;
import com.aegismesh.stock.dto.PharmacyResponse;
import com.aegismesh.stock.entity.StockReservation;
import com.aegismesh.stock.entity.MedicineStock;
import com.aegismesh.stock.entity.ReservationItem;
import com.aegismesh.stock.entity.ReservationStatus;
import com.aegismesh.stock.entity.Pharmacy;
import com.aegismesh.stock.repository.StockReservationRepository;
import com.aegismesh.stock.repository.MedicineStockRepository;
import com.aegismesh.stock.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final PharmacyRepository pharmacyRepository;
    private final MedicineStockRepository stockRepository;
    private final StockReservationRepository reservationRepository;


    // =========================================================
    // PHARMACY
    // =========================================================

    @Transactional
    public PharmacyResponse createPharmacy(
            CreatePharmacyRequest request
    ) {

        if (pharmacyRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pharmacy already exists: " + request.code()
            );
        }

        Pharmacy pharmacy = Pharmacy.builder()
                .code(request.code())
                .name(request.name())
                .location(request.location())
                .active(true)
                .build();

        Pharmacy savedPharmacy =
                pharmacyRepository.save(pharmacy);

        return toPharmacyResponse(savedPharmacy);
    }


    @Transactional(readOnly = true)
    public List<PharmacyResponse> getPharmacies() {

        return pharmacyRepository.findAll()
                .stream()
                .map(this::toPharmacyResponse)
                .toList();
    }


    // =========================================================
    // STOCK
    // =========================================================

    @Transactional
    public StockResponse upsertStock(
            UpsertStockRequest request
    ) {

        Pharmacy pharmacy =
                pharmacyRepository
                        .findByCode(request.pharmacyCode())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Pharmacy not found: "
                                                + request.pharmacyCode()
                                )
                        );

        MedicineStock stock =
                stockRepository
                        .findByPharmacy_CodeAndMedicineCode(
                                request.pharmacyCode(),
                                request.medicineCode()
                        )
                        .orElseGet(() ->
                                MedicineStock.builder()
                                        .pharmacy(pharmacy)
                                        .medicineCode(request.medicineCode())
                                        .reservedQuantity(0)
                                        .build()
                        );

        /*
         * Example:
         *
         * onHand = 10
         * reserved = 7
         *
         * We must not allow:
         *
         * onHand = 5
         *
         * because then available would become -2.
         */
        if (request.onHandQuantity()
                < stock.getReservedQuantity()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "On-hand quantity cannot be lower "
                            + "than reserved quantity"
            );
        }

        stock.setMedicineName(
                request.medicineName()
        );

        stock.setOnHandQuantity(
                request.onHandQuantity()
        );

        MedicineStock savedStock =
                stockRepository.save(stock);

        return toStockResponse(savedStock);
    }


    @Transactional(readOnly = true)
    public List<StockResponse> getStockByMedicine(
            String medicineCode
    ) {

        return stockRepository
                .findByMedicineCodeOrderByPharmacy_Code(medicineCode)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<StockResponse> getPharmacyStock(
            String pharmacyCode
    ) {

        if (!pharmacyRepository.existsByCode(
                pharmacyCode
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pharmacy not found: "
                            + pharmacyCode
            );
        }

        return stockRepository
                .findByPharmacy_Code(pharmacyCode)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }


    // =========================================================
    // CREATE STOCK RESERVATION
    // =========================================================

    @Transactional
    public ReservationResponse createReservation(
            CreateReservationRequest request
    ) {

        /*
         * Prevent requests like:
         *
         * PARACETAMOL x 5
         * PARACETAMOL x 7
         *
         * being sent in the same reservation.
         */
        validateNoDuplicateMedicines(request.items());

        Pharmacy pharmacy =
                pharmacyRepository
                        .findByCode(request.pharmacyCode())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Pharmacy not found: "
                                                + request.pharmacyCode()
                                )
                        );

        if (!pharmacy.isActive()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pharmacy is inactive: "
                            + pharmacy.getCode()
            );
        }

        /*
         * Sort by medicine code before locking.
         *
         * This helps multiple transactions acquire locks
         * in a consistent order and reduces deadlock risk.
         */
        List<ReservationItemRequest> sortedItems =
                request.items()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        ReservationItemRequest::medicineCode
                                )
                        )
                        .toList();


        /*
         * Stores the stock rows that we successfully lock.
         *
         * Example:
         *
         * PARACETAMOL -> MedicineStock
         * AMOXICILLIN -> MedicineStock
         */
        Map<String, MedicineStock> lockedStocks =
                new LinkedHashMap<>();

        String rejectionReason = null;


        // -----------------------------------------------------
        // CHECK AND LOCK ALL REQUIRED STOCK
        // -----------------------------------------------------

        for (ReservationItemRequest item : sortedItems) {

            Optional<MedicineStock> stockOptional =
                    stockRepository.findForUpdate(
                            pharmacy.getCode(),
                            item.medicineCode()
                    );

            /*
             * Medicine does not exist at this pharmacy.
             */
            if (stockOptional.isEmpty()) {

                rejectionReason =
                        "Medicine "
                                + item.medicineCode()
                                + " does not exist at pharmacy "
                                + pharmacy.getCode();

                break;
            }


            MedicineStock stock =
                    stockOptional.get();

            lockedStocks.put(
                    item.medicineCode(),
                    stock
            );


            /*
             * Medicine exists, but there isn't enough
             * available stock.
             */
            if (!stock.canReserve(item.quantity())) {

                rejectionReason =
                        "Insufficient stock for medicine "
                                + item.medicineCode()
                                + " at pharmacy "
                                + pharmacy.getCode()
                                + ". Requested="
                                + item.quantity()
                                + ", Available="
                                + stock.getAvailableQuantity();

                break;
            }
        }


        // -----------------------------------------------------
        // CREATE RESERVATION RECORD
        // -----------------------------------------------------

        StockReservation reservation =
                StockReservation.builder()
                        .reservationNumber(
                                generateReservationNumber()
                        )
                        .requestId(request.requestId())
                        .pharmacy(pharmacy)
                        .build();


        /*
         * Copy requested medicines into reservation items.
         */
        for (ReservationItemRequest item : request.items()) {

            ReservationItem reservationItem =
                    ReservationItem.builder()
                            .medicineCode(item.medicineCode())
                            .quantity(item.quantity())
                            .build();

            reservation.addItem(reservationItem);
        }


        // -----------------------------------------------------
        // RESERVATION FAILED
        // -----------------------------------------------------

        if (rejectionReason != null) {

            reservation.setStatus(
                    ReservationStatus.REJECTED
            );

            reservation.setRejectionReason(
                    rejectionReason
            );

            StockReservation savedReservation =
                    reservationRepository.save(reservation);

            return toReservationResponse(
                    savedReservation
            );
        }


        // -----------------------------------------------------
        // RESERVATION SUCCESSFUL
        // -----------------------------------------------------

        for (ReservationItemRequest item : sortedItems) {

            MedicineStock stock =
                    lockedStocks.get(item.medicineCode());

            stock.reserve(
                    item.quantity()
            );
        }


        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        reservation.setRejectionReason(null);

        StockReservation savedReservation =
                reservationRepository.save(reservation);

        return toReservationResponse(
                savedReservation
        );
    }


    // =========================================================
    // RELEASE RESERVATION
    // =========================================================

    @Transactional
    public ReservationResponse releaseReservation(
            UUID reservationId
    ) {

        StockReservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Reservation not found: "
                                                + reservationId
                                )
                        );


        /*
         * Make release idempotent.
         *
         * Calling release twice will not subtract the
         * reserved quantity twice.
         */
        if (reservation.getStatus()
                == ReservationStatus.RELEASED) {

            return toReservationResponse(
                    reservation
            );
        }


        /*
         * REJECTED reservations never reserved anything,
         * therefore there is nothing to release.
         */
        if (reservation.getStatus()
                == ReservationStatus.REJECTED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Rejected reservation cannot be released"
            );
        }


        /*
         * Lock stock rows in a consistent order.
         */
        List<ReservationItem> sortedItems =
                reservation.getItems()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        ReservationItem::getMedicineCode
                                )
                        )
                        .toList();


        for (ReservationItem item : sortedItems) {

            MedicineStock stock =
                    stockRepository
                            .findForUpdate(
                                    reservation
                                            .getPharmacy()
                                            .getCode(),
                                    item.getMedicineCode()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.CONFLICT,
                                            "Stock not found for medicine "
                                                    + item.getMedicineCode()
                                    )
                            );


            stock.release(
                    item.getQuantity()
            );
        }


        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        StockReservation savedReservation =
                reservationRepository.save(reservation);

        return toReservationResponse(
                savedReservation
        );
    }


    // =========================================================
    // GET RESERVATION
    // =========================================================

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(
            String reservationNumber
    ) {

        StockReservation reservation =
                reservationRepository
                        .findByReservationNumber(
                                reservationNumber
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Reservation not found: "
                                                + reservationNumber
                                )
                        );

        return toReservationResponse(
                reservation
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateNoDuplicateMedicines(
            List<ReservationItemRequest> items
    ) {

        Set<String> seenMedicines =
                new HashSet<>();

        for (ReservationItemRequest item : items) {

            if (!seenMedicines.add(item.medicineCode())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate medicine in reservation request: "
                                + item.medicineCode()
                );
            }
        }
    }


    // =========================================================
    // GENERATE RESERVATION NUMBER
    // =========================================================

    private String generateReservationNumber() {

        return "RES-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }


    // =========================================================
    // DTO MAPPERS
    // =========================================================

    private PharmacyResponse toPharmacyResponse(
            Pharmacy pharmacy
    ) {

        return new PharmacyResponse(
                pharmacy.getId(),
                pharmacy.getCode(),
                pharmacy.getName(),
                pharmacy.getLocation(),
                pharmacy.isActive()
        );
    }


    private StockResponse toStockResponse(
            MedicineStock stock
    ) {

        return new StockResponse(
                stock.getId(),
                stock.getPharmacy().getCode(),
                stock.getPharmacy().getName(),
                stock.getMedicineCode(),
                stock.getMedicineName(),
                stock.getOnHandQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity()
        );
    }


    private ReservationResponse toReservationResponse(
            StockReservation reservation
    ) {

        List<ReservationItemResponse> items =
                reservation.getItems()
                        .stream()
                        .map(item ->
                                new ReservationItemResponse(
                                        item.getId(),
                                        item.getMedicineCode(),
                                        item.getQuantity()
                                )
                        )
                        .toList();


        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getRequestId(),
                reservation.getPharmacy().getCode(),
                reservation.getStatus(),
                reservation.getRejectionReason(),
                items,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}