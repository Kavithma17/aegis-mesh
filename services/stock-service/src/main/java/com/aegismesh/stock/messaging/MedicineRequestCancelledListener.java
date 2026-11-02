package com.aegismesh.stock.messaging;

import com.aegismesh.stock.event.MedicineRequestCancelledEvent;
import com.aegismesh.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * Saga compensation: release stock when a patient cancels.
 *
 * The consumer's default event type in
 * application.properties is MedicineRequestCreatedEvent,
 * so this listener overrides it for its own topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineRequestCancelledListener {

    private final StockService stockService;

    @KafkaListener(
            topics = KafkaTopics.REQUEST_CANCELLED,
            properties = {
                    "spring.json.value.default.type="
                            + "com.aegismesh.stock.event.MedicineRequestCancelledEvent"
            }
    )
    public void onRequestCancelled(
            MedicineRequestCancelledEvent event
    ) {

        log.info(
                "Request {} was cancelled, releasing its stock",
                event.requestNumber()
        );

        stockService.releaseReservationForRequest(
                event.requestId()
        );
    }
}
