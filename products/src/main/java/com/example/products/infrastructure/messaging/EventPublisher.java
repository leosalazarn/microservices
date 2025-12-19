package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.ProductCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
            
            Message<String> message = MessageBuilder
                .withPayload(eventJson)
                .setHeader(KafkaHeaders.TOPIC, PRODUCT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "product.created")
                .setHeader("eventType", "ProductCreatedEvent")
                .build();
                
            kafkaTemplate.send(message);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductCreatedEvent: " + e.getMessage());
        }
    }
}
