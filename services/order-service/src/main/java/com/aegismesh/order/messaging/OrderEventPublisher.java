package com.aegismesh.order.messaging;

import com.aegismesh.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private static final String ORDER_CREATED_TOPIC =
            "aegis.order.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(
            OrderCreatedEvent event
    ) {

        kafkaTemplate.send(
                ORDER_CREATED_TOPIC,
                event.orderId().toString(),
                event
        );
    }
}