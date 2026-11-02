package com.aegismesh.stock.messaging;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * What happens when a listener throws.
 *
 * 1. Retry with exponential backoff: 1s, 2s, 4s.
 *    Fixes temporary problems such as the database
 *    being down for a few seconds.
 *
 * 2. Still failing: publish the message to a dead-letter
 *    topic (original topic + "-dlt") instead of dropping
 *    it. The exception is stored in the record headers so
 *    the message can be inspected and replayed later.
 *
 * Messages that cannot be deserialized are never retried
 * (retrying cannot fix broken JSON) and go straight to
 * the dead-letter topic.
 *
 * Spring Boot plugs this error handler into every
 * @KafkaListener automatically.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> jsonTemplate,
            ProducerFactory<String, Object> producerFactory
    ) {

        /*
         * A message that failed to deserialize has no Java
         * object, only its raw bytes. Send those bytes as-is
         * with a byte[] template; send everything else with
         * the normal JSON template.
         */
        KafkaTemplate<String, byte[]> bytesTemplate =
                new KafkaTemplate<>(
                        new DefaultKafkaProducerFactory<>(
                                producerFactory.getConfigurationProperties(),
                                new StringSerializer(),
                                new ByteArraySerializer()
                        )
                );

        Map<Class<?>, KafkaOperations<?, ?>> templates =
                new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, jsonTemplate);

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(templates);

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
