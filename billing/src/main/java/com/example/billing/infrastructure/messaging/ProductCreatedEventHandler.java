package com.example.billing.infrastructure.messaging;

import com.example.billing.domain.event.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductCreatedEventHandler implements EventHandler<ProductEvent> {
    
    @Override
    public void handle(ProductEvent event) {
        log.info("Processing product created: {} with price: {}", event.getName(), event.getPrice());
        
        // Business logic for handling new product
        // Example: Could create default pricing rules, update catalogs, etc.
        // This is where SAGA compensation logic would go
    }
    
    @Override
    public String getEventType() {
        return "ProductCreatedEvent";
    }
}
