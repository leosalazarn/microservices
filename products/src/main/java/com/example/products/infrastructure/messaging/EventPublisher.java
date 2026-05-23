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
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            Message<String> message = MessageBuilder
                .withPayload(eventJson)
                .setHeader(KafkaHeaders.TOPIC, PRODUCT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "product.created")
                .setHeader("eventType", "ProductCreatedEvent")
                .build();
                
            kafkaTemplate.send(message);
        } catch (Exception e) {
            log.error("Failed to publish ProductCreatedEvent", e);
        }
    }

    public void publishProductUpdatedEvent(ProductUpdatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            Message<String> message = MessageBuilder
                .withPayload(eventJson)
                .setHeader(KafkaHeaders.TOPIC, PRODUCT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "product.updated")
                .setHeader("eventType", "ProductUpdatedEvent")
                .build();

            kafkaTemplate.send(message);
        } catch (Exception e) {
            log.error("Failed to publish ProductUpdatedEvent", e);
        }
    }
}
