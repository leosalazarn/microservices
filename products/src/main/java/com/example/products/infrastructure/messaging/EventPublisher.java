package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.ProductCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String PRODUCT_EVENTS_TOPIC = "product-events";
    
    public void publishProductCreatedEvent(ProductCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PRODUCT_EVENTS_TOPIC, "product.created", eventJson);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductCreatedEvent: " + e.getMessage());
        }
    }
}
