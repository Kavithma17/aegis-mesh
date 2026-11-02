package com.aegismesh.request.messaging;

import com.aegismesh.request.event.MedicineRequestCancelledEvent;
import com.aegismesh.request.event.MedicineRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRequestCreated(
            MedicineRequestCreatedEvent event
    ) {

        kafkaTemplate.send(
                KafkaTopics.REQUEST_CREATED,
                event.requestId().toString(),
                event
        );
    }

    public void publishRequestCancelled(
            MedicineRequestCancelledEvent event
    ) {

        kafkaTemplate.send(
                KafkaTopics.REQUEST_CANCELLED,
                event.requestId().toString(),
                event
        );
    }
}
