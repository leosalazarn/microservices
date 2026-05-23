package com.example.products.infrastructure.cache;

import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationEventHandler {

    @EventListener
    @CacheEvict(value = "products", key = "'all'")
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Cache invalidated due to ProductCreatedEvent for product: {}", event.getProductId());
    }

    @EventListener
    @CacheEvict(value = "products", key = "'all'")
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("Cache invalidated due to ProductUpdatedEvent for product: {}", event.getAggregateId());
    }
}
