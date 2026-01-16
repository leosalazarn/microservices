package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    
    private final EventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
    
    private void publish(DomainEvent event) {
        // Publish to Kafka for SAGA orchestration
        if (event instanceof ProductCreatedEvent) {
            eventPublisher.publishProductCreatedEvent((ProductCreatedEvent) event);
        }
        
        // Publish locally for event-driven side effects (cache invalidation, etc.)
        applicationEventPublisher.publishEvent(event);
    }
}
