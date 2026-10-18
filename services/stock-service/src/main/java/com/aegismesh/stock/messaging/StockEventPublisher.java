package com.aegismesh.stock.messaging;

import com.aegismesh.stock.event.StockReservationResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /*
     * Wait for the broker to acknowledge the send.
     *
     * If publishing fails, the exception reaches the
     * listener's error handler and the request event is
     * retried. Because reservations are idempotent, the
     * retry only re-publishes the result.
     */
    public void publishReservationResult(
            StockReservationResultEvent event
    ) {

        kafkaTemplate.send(
                KafkaTopics.RESERVATION_RESULT,
                event.requestId().toString(),
                event
        ).join();
    }
}
