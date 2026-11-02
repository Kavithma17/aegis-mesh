package com.aegismesh.request.messaging;

import com.aegismesh.request.event.StockReservationResultEvent;
import com.aegismesh.request.service.MedicineRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * Saga step 4: close the loop.
 *
 * The Stock Service tells us whether stock was reserved,
 * and we move the request to STOCK_RESERVED or
 * STOCK_REJECTED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationResultListener {

    private final MedicineRequestService requestService;

    @KafkaListener(topics = KafkaTopics.RESERVATION_RESULT)
    public void onReservationResult(
            StockReservationResultEvent event
    ) {

        log.info(
                "Received reservation result {} for request {}",
                event.status(),
                event.requestId()
        );

        requestService.applyReservationResult(event);
    }
}
