package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.event.ProductUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";

    public void publishProductCreatedEvent(ProductCreatedEvent event) {
        if (event == null) {
            log.error("Cannot publish null ProductCreatedEvent");
            return;
        }
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            Message<String> message = MessageBuilder
                    .withPayload(eventJson)
                    .setHeader(KafkaHeaders.TOPIC, PRODUCT_EVENTS_TOPIC)
                    .setHeader(KafkaHeaders.KEY, "product.created")
                    .setHeader("eventType", "ProductCreatedEvent")
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send ProductCreatedEvent for product {}", event.getProductId(), ex);
                } else {
                    log.debug("ProductCreatedEvent sent for product {} at offset {}",
                            event.getProductId(), result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize ProductCreatedEvent for product {}", event.getProductId(), e);
        }
    }

    public void publishProductUpdatedEvent(ProductUpdatedEvent event) {
        if (event == null) {
            log.error("Cannot publish null ProductUpdatedEvent");
            return;
        }
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            Message<String> message = MessageBuilder
                    .withPayload(eventJson)
                    .setHeader(KafkaHeaders.TOPIC, PRODUCT_EVENTS_TOPIC)
                    .setHeader(KafkaHeaders.KEY, "product.updated")
                    .setHeader("eventType", "ProductUpdatedEvent")
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send ProductUpdatedEvent for product {}", event.getProductId(), ex);
                } else {
                    log.debug("ProductUpdatedEvent sent for product {} at offset {}",
                            event.getProductId(), result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize ProductUpdatedEvent for product {}", event.getProductId(), e);
        }
    }
}
