package com.example.billing.infrastructure.messaging;

import com.example.billing.domain.event.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {
    
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "product-events", groupId = "billing-service")
    public void handleProductCreated(String message) {
        try {
            ProductEvent event = objectMapper.readValue(message, ProductEvent.class);
            log.info("Received product created event: {}", event);
            
            // Process the event - could trigger invoice creation, pricing updates, etc.
            processProductCreatedEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to process product event: {}", e.getMessage());
        }
    }
    
    private void processProductCreatedEvent(ProductEvent event) {
        // Business logic for handling new product
        log.info("Processing product created: {} with price: {}", event.getName(), event.getPrice());
        
        // Example: Could create default pricing rules, update catalogs, etc.
        // This is where SAGA compensation logic would go
    }
}
