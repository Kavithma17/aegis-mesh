package com.aegismesh.request.messaging;

import com.aegismesh.request.event.MedicineRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestEventPublisher {

    private static final String REQUEST_CREATED_TOPIC =
            "aegis.medicine-request.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRequestCreated(
            MedicineRequestCreatedEvent event
    ) {

        kafkaTemplate.send(
                REQUEST_CREATED_TOPIC,
                event.requestId().toString(),
                event
        );
    }
}