package com.example.products.infrastructure.cache;

import com.example.products.domain.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event-driven cache invalidation handler.
 * Listens to domain events and invalidates cache accordingly.
 * This maintains SAGA pattern compliance by reacting to events rather than direct coupling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationEventHandler {

    @EventListener
    @CacheEvict(value = "products", key = "'all'")
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Cache invalidated due to ProductCreatedEvent for product: {}", event.getProductId());
    }
}
