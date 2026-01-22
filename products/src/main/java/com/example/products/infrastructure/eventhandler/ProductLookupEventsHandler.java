package com.example.products.infrastructure.eventhandler;

import com.example.products.domain.entity.ProductLookupEntity;
import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.repository.ProductLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductLookupEventsHandler {

    private final ProductLookupRepository lookupRepository;

    @EventListener
    public void on(ProductCreatedEvent event) {
        log.info("Persisting product lookup for: {}", event.getName());
        ProductLookupEntity lookup = new ProductLookupEntity(event.getProductId(), event.getName());
        lookupRepository.save(lookup);
    }
}
