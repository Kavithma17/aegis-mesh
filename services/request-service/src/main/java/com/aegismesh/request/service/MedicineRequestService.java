package com.aegismesh.request.service;

import com.aegismesh.request.dto.CreateMedicineRequestDto;
import com.aegismesh.request.dto.MedicineRequestItemDto;
import com.aegismesh.request.dto.MedicineRequestItemResponse;
import com.aegismesh.request.dto.MedicineRequestResponse;
import com.aegismesh.request.entity.MedicineRequest;
import com.aegismesh.request.entity.MedicineRequestItem;
import com.aegismesh.request.entity.RequestStatus;
import com.aegismesh.request.event.MedicineRequestCancelledEvent;
import com.aegismesh.request.event.MedicineRequestCreatedEvent;
import com.aegismesh.request.event.RequestItemEvent;
import com.aegismesh.request.event.StockReservationResultEvent;
import com.aegismesh.request.messaging.RequestEventPublisher;
import com.aegismesh.request.repository.MedicineRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineRequestService {

    private final MedicineRequestRepository requestRepository;
    private final RequestEventPublisher requestEventPublisher;

    @Transactional
    public MedicineRequestResponse createRequest(CreateMedicineRequestDto dto) {

        BigDecimal totalAmount = calculateTotal(dto.items());

        MedicineRequest request = MedicineRequest.builder()
                .requestNumber(generateRequestNumber())
                .patientId(dto.patientId())
                .pharmacyCode(dto.pharmacyCode())
                .status(RequestStatus.CREATED)
                .totalAmount(totalAmount)
                .build();

        for (MedicineRequestItemDto itemDto : dto.items()) {

            MedicineRequestItem requestItem = MedicineRequestItem.builder()
                    .medicineCode(itemDto.medicineCode())
                    .medicineName(itemDto.medicineName())
                    .quantity(itemDto.quantity())
                    .unitPrice(
                            itemDto.unitPrice()
                                    .setScale(2, RoundingMode.HALF_UP)
                    )
                    .build();

            request.addItem(requestItem);
        }

        MedicineRequest savedRequest = requestRepository.save(request);

        MedicineRequestCreatedEvent event =
                new MedicineRequestCreatedEvent(
                        UUID.randomUUID(),
                        savedRequest.getId(),
                        savedRequest.getRequestNumber(),
                        savedRequest.getPatientId(),
                        savedRequest.getPharmacyCode(),
                        savedRequest.getItems()
                                .stream()
                                .map(item ->
                                        new RequestItemEvent(
                                                item.getMedicineCode(),
                                                item.getQuantity()
                                        )
                                )
                                .toList(),
                        Instant.now()
                );

        requestEventPublisher.publishRequestCreated(event);

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public MedicineRequestResponse getRequest(UUID requestId) {

        MedicineRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Medicine request not found: " + requestId
                ));

        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public MedicineRequestResponse getRequestByNumber(String requestNumber) {

        MedicineRequest request = requestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Medicine request not found: " + requestNumber
                ));

        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<MedicineRequestResponse> getAllRequests() {

        return requestRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MedicineRequestResponse cancelRequest(UUID requestId) {

        MedicineRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Medicine request not found: " + requestId
                ));

        if (request.getStatus() == RequestStatus.CANCELLED) {
            return toResponse(request);
        }

        RequestStatus previousStatus = request.getStatus();

        request.setStatus(RequestStatus.CANCELLED);

        MedicineRequest savedRequest = requestRepository.save(request);

        /*
         * Saga compensation.
         *
         * A rejected request never held stock, so there is
         * nothing to release. In every other state the Stock
         * Service may hold (or be about to hold) stock for
         * this request, so ask it to release.
         */
        if (previousStatus != RequestStatus.STOCK_REJECTED) {
            publishCancelled(savedRequest);
        }

        return toResponse(savedRequest);
    }

    /*
     * Saga step 4: apply the Stock Service's answer.
     *
     * Kafka delivers at least once, so this must be safe to
     * run more than once for the same request.
     */
    @Transactional
    public void applyReservationResult(StockReservationResultEvent event) {

        MedicineRequest request = requestRepository
                .findById(event.requestId())
                .orElse(null);

        if (request == null) {
            log.warn(
                    "Ignoring reservation result for unknown request {}",
                    event.requestId()
            );
            return;
        }

        boolean reserved = "RESERVED".equals(event.status());

        /*
         * Race: the patient cancelled while the Stock Service
         * was still reserving. The cancelled event may have
         * been handled before the reservation existed, so the
         * stock is now held for a cancelled request.
         * Compensate again; the release is idempotent.
         */
        if (request.getStatus() == RequestStatus.CANCELLED) {

            if (reserved) {
                log.info(
                        "Request {} was cancelled during reservation, releasing stock",
                        request.getRequestNumber()
                );
                publishCancelled(request);
            }
            return;
        }

        /*
         * Only a request still waiting for stock can move.
         * A redelivered result for a request that already
         * moved is ignored.
         */
        if (request.getStatus() != RequestStatus.CREATED
                && request.getStatus() != RequestStatus.STOCK_PENDING) {
            return;
        }

        request.setReservationNumber(event.reservationNumber());

        if (reserved) {
            request.setStatus(RequestStatus.STOCK_RESERVED);
            request.setStatusReason(null);
        } else {
            request.setStatus(RequestStatus.STOCK_REJECTED);
            request.setStatusReason(event.reason());
        }

        requestRepository.save(request);

        log.info(
                "Request {} is now {}",
                request.getRequestNumber(),
                request.getStatus()
        );
    }

    private void publishCancelled(MedicineRequest request) {

        requestEventPublisher.publishRequestCancelled(
                new MedicineRequestCancelledEvent(
                        UUID.randomUUID(),
                        request.getId(),
                        request.getRequestNumber(),
                        Instant.now()
                )
        );
    }

    private BigDecimal calculateTotal(List<MedicineRequestItemDto> items) {

        return items.stream()
                .map(item ->
                        item.unitPrice()
                                .multiply(
                                        BigDecimal.valueOf(item.quantity())
                                )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateRequestNumber() {

        String requestNumber;

        do {
            requestNumber =
                    "MED-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();

        } while (requestRepository.existsByRequestNumber(requestNumber));

        return requestNumber;
    }

    private MedicineRequestResponse toResponse(MedicineRequest request) {

        List<MedicineRequestItemResponse> items = request.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new MedicineRequestResponse(
                request.getId(),
                request.getRequestNumber(),
                request.getPatientId(),
                request.getPharmacyCode(),
                request.getStatus(),
                request.getReservationNumber(),
                request.getStatusReason(),
                request.getTotalAmount(),
                items,
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    private MedicineRequestItemResponse toItemResponse(MedicineRequestItem item) {

        return new MedicineRequestItemResponse(
                item.getId(),
                item.getMedicineCode(),
                item.getMedicineName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}