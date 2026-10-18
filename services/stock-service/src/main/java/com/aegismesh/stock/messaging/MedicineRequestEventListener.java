package com.aegismesh.stock.messaging;

import com.aegismesh.stock.dto.CreateReservationRequest;
import com.aegismesh.stock.dto.ReservationItemRequest;
import com.aegismesh.stock.dto.ReservationResponse;
import com.aegismesh.stock.entity.ReservationStatus;
import com.aegismesh.stock.event.MedicineRequestCreatedEvent;
import com.aegismesh.stock.event.StockReservationResultEvent;
import com.aegismesh.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineRequestEventListener {

    private final StockService stockService;
    private final StockEventPublisher stockEventPublisher;

    /*
     * Reserve stock automatically when a patient creates
     * a medicine request in the Request Service, then tell
     * the Request Service how it went.
     */
    @KafkaListener(topics = KafkaTopics.REQUEST_CREATED)
    public void onRequestCreated(
            MedicineRequestCreatedEvent event
    ) {

        log.info(
                "Received request {} for pharmacy {}",
                event.requestNumber(),
                event.pharmacyCode()
        );

        StockReservationResultEvent result;

        try {

            ReservationResponse reservation =
                    stockService.createReservation(
                            toReservationRequest(event)
                    );

            result = new StockReservationResultEvent(
                    UUID.randomUUID(),
                    event.requestId(),
                    reservation.reservationNumber(),
                    reservation.status(),
                    reservation.rejectionReason(),
                    Instant.now()
            );

        } catch (ResponseStatusException ex) {

            /*
             * Business errors (unknown or inactive pharmacy,
             * duplicate medicines) will never succeed on retry,
             * so reject the request instead of retrying.
             */
            log.warn(
                    "Rejecting request {}: {}",
                    event.requestNumber(),
                    ex.getReason()
            );

            result = new StockReservationResultEvent(
                    UUID.randomUUID(),
                    event.requestId(),
                    null,
                    ReservationStatus.REJECTED,
                    ex.getReason(),
                    Instant.now()
            );
        }

        stockEventPublisher.publishReservationResult(result);

        log.info(
                "Reservation {} for request {} is {}",
                result.reservationNumber(),
                event.requestNumber(),
                result.status()
        );
    }


    private CreateReservationRequest toReservationRequest(
            MedicineRequestCreatedEvent event
    ) {

        List<ReservationItemRequest> items =
                event.items()
                        .stream()
                        .map(item ->
                                new ReservationItemRequest(
                                        item.medicineCode(),
                                        item.quantity()
                                )
                        )
                        .toList();

        return new CreateReservationRequest(
                event.requestId(),
                event.pharmacyCode(),
                items
        );
    }
}
