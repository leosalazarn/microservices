package com.example.products.infrastructure.cache;

import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationEventHandler {

    private final CacheManager cacheManager;

    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        evictProductsCache();
        log.debug("Cache invalidated due to ProductCreatedEvent for product: {}", event.getProductId());
    }

    @EventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        evictProductsCache();
        log.debug("Cache invalidated due to ProductUpdatedEvent for product: {}", event.getAggregateId());
    }

    private void evictProductsCache() {
        Cache cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.clear();
        }
    }
}
