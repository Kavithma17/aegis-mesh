package com.aegismesh.inventory_service.service;

import com.aegismesh.inventory_service.dto.CreateReservationRequest;
import com.aegismesh.inventory_service.dto.CreateWarehouseRequest;
import com.aegismesh.inventory_service.dto.ReservationItemRequest;
import com.aegismesh.inventory_service.dto.ReservationItemResponse;
import com.aegismesh.inventory_service.dto.ReservationResponse;
import com.aegismesh.inventory_service.dto.StockResponse;
import com.aegismesh.inventory_service.dto.UpsertStockRequest;
import com.aegismesh.inventory_service.dto.WarehouseResponse;
import com.aegismesh.inventory_service.entity.InventoryReservation;
import com.aegismesh.inventory_service.entity.InventoryStock;
import com.aegismesh.inventory_service.entity.ReservationItem;
import com.aegismesh.inventory_service.entity.ReservationStatus;
import com.aegismesh.inventory_service.entity.Warehouse;
import com.aegismesh.inventory_service.repository.InventoryReservationRepository;
import com.aegismesh.inventory_service.repository.InventoryStockRepository;
import com.aegismesh.inventory_service.repository.WarehouseRepository;
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
public class InventoryService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;


    // =========================================================
    // WAREHOUSE
    // =========================================================

    @Transactional
    public WarehouseResponse createWarehouse(
            CreateWarehouseRequest request
    ) {

        if (warehouseRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Warehouse already exists: " + request.code()
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.code())
                .name(request.name())
                .location(request.location())
                .active(true)
                .build();

        Warehouse savedWarehouse =
                warehouseRepository.save(warehouse);

        return toWarehouseResponse(savedWarehouse);
    }


    @Transactional(readOnly = true)
    public List<WarehouseResponse> getWarehouses() {

        return warehouseRepository.findAll()
                .stream()
                .map(this::toWarehouseResponse)
                .toList();
    }


    // =========================================================
    // STOCK
    // =========================================================

    @Transactional
    public StockResponse upsertStock(
            UpsertStockRequest request
    ) {

        Warehouse warehouse =
                warehouseRepository
                        .findByCode(request.warehouseCode())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Warehouse not found: "
                                                + request.warehouseCode()
                                )
                        );

        InventoryStock stock =
                stockRepository
                        .findByWarehouse_CodeAndSku(
                                request.warehouseCode(),
                                request.sku()
                        )
                        .orElseGet(() ->
                                InventoryStock.builder()
                                        .warehouse(warehouse)
                                        .sku(request.sku())
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

        stock.setProductName(
                request.productName()
        );

        stock.setOnHandQuantity(
                request.onHandQuantity()
        );

        InventoryStock savedStock =
                stockRepository.save(stock);

        return toStockResponse(savedStock);
    }


    @Transactional(readOnly = true)
    public List<StockResponse> getStockBySku(
            String sku
    ) {

        return stockRepository
                .findBySkuOrderByWarehouse_Code(sku)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<StockResponse> getWarehouseStock(
            String warehouseCode
    ) {

        if (!warehouseRepository.existsByCode(
                warehouseCode
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Warehouse not found: "
                            + warehouseCode
            );
        }

        return stockRepository
                .findByWarehouse_Code(warehouseCode)
                .stream()
                .map(this::toStockResponse)
                .toList();
    }


    // =========================================================
    // CREATE INVENTORY RESERVATION
    // =========================================================

    @Transactional
    public ReservationResponse createReservation(
            CreateReservationRequest request
    ) {

        /*
         * Prevent requests like:
         *
         * LAPTOP x 5
         * LAPTOP x 7
         *
         * being sent in the same reservation.
         */
        validateNoDuplicateSkus(request.items());

        Warehouse warehouse =
                warehouseRepository
                        .findByCode(request.warehouseCode())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Warehouse not found: "
                                                + request.warehouseCode()
                                )
                        );

        if (!warehouse.isActive()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Warehouse is inactive: "
                            + warehouse.getCode()
            );
        }

        /*
         * Sort by SKU before locking.
         *
         * This helps multiple transactions acquire locks
         * in a consistent order and reduces deadlock risk.
         */
        List<ReservationItemRequest> sortedItems =
                request.items()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        ReservationItemRequest::sku
                                )
                        )
                        .toList();


        /*
         * Stores the inventory rows that we successfully lock.
         *
         * Example:
         *
         * LAPTOP -> InventoryStock
         * MOUSE  -> InventoryStock
         */
        Map<String, InventoryStock> lockedStocks =
                new LinkedHashMap<>();

        String rejectionReason = null;


        // -----------------------------------------------------
        // CHECK AND LOCK ALL REQUIRED STOCK
        // -----------------------------------------------------

        for (ReservationItemRequest item : sortedItems) {

            Optional<InventoryStock> stockOptional =
                    stockRepository.findForUpdate(
                            warehouse.getCode(),
                            item.sku()
                    );

            /*
             * Product does not exist at this warehouse.
             */
            if (stockOptional.isEmpty()) {

                rejectionReason =
                        "SKU "
                                + item.sku()
                                + " does not exist at warehouse "
                                + warehouse.getCode();

                break;
            }


            InventoryStock stock =
                    stockOptional.get();

            lockedStocks.put(
                    item.sku(),
                    stock
            );


            /*
             * Product exists, but there isn't enough
             * available inventory.
             */
            if (!stock.canReserve(item.quantity())) {

                rejectionReason =
                        "Insufficient stock for SKU "
                                + item.sku()
                                + " at warehouse "
                                + warehouse.getCode()
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

        InventoryReservation reservation =
                InventoryReservation.builder()
                        .reservationNumber(
                                generateReservationNumber()
                        )
                        .orderId(request.orderId())
                        .warehouse(warehouse)
                        .build();


        /*
         * Copy requested products into reservation items.
         */
        for (ReservationItemRequest item : request.items()) {

            ReservationItem reservationItem =
                    ReservationItem.builder()
                            .sku(item.sku())
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

            InventoryReservation savedReservation =
                    reservationRepository.save(reservation);

            return toReservationResponse(
                    savedReservation
            );
        }


        // -----------------------------------------------------
        // RESERVATION SUCCESSFUL
        // -----------------------------------------------------

        for (ReservationItemRequest item : sortedItems) {

            InventoryStock stock =
                    lockedStocks.get(item.sku());

            stock.reserve(
                    item.quantity()
            );
        }


        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        reservation.setRejectionReason(null);

        InventoryReservation savedReservation =
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

        InventoryReservation reservation =
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
         * Lock inventory rows in a consistent order.
         */
        List<ReservationItem> sortedItems =
                reservation.getItems()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        ReservationItem::getSku
                                )
                        )
                        .toList();


        for (ReservationItem item : sortedItems) {

            InventoryStock stock =
                    stockRepository
                            .findForUpdate(
                                    reservation
                                            .getWarehouse()
                                            .getCode(),
                                    item.getSku()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.CONFLICT,
                                            "Stock not found for SKU "
                                                    + item.getSku()
                                    )
                            );


            stock.release(
                    item.getQuantity()
            );
        }


        reservation.setStatus(
                ReservationStatus.RELEASED
        );

        InventoryReservation savedReservation =
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

        InventoryReservation reservation =
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

    private void validateNoDuplicateSkus(
            List<ReservationItemRequest> items
    ) {

        Set<String> seenSkus =
                new HashSet<>();

        for (ReservationItemRequest item : items) {

            if (!seenSkus.add(item.sku())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate SKU in reservation request: "
                                + item.sku()
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

    private WarehouseResponse toWarehouseResponse(
            Warehouse warehouse
    ) {

        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.isActive()
        );
    }


    private StockResponse toStockResponse(
            InventoryStock stock
    ) {

        return new StockResponse(
                stock.getId(),
                stock.getWarehouse().getCode(),
                stock.getWarehouse().getName(),
                stock.getSku(),
                stock.getProductName(),
                stock.getOnHandQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity()
        );
    }


    private ReservationResponse toReservationResponse(
            InventoryReservation reservation
    ) {

        List<ReservationItemResponse> items =
                reservation.getItems()
                        .stream()
                        .map(item ->
                                new ReservationItemResponse(
                                        item.getId(),
                                        item.getSku(),
                                        item.getQuantity()
                                )
                        )
                        .toList();


        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getOrderId(),
                reservation.getWarehouse().getCode(),
                reservation.getStatus(),
                reservation.getRejectionReason(),
                items,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}