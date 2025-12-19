package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    
    private final EventPublisher eventPublisher;
    
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
    
    private void publish(DomainEvent event) {
        if (event instanceof ProductCreatedEvent) {
            eventPublisher.publishProductCreatedEvent((ProductCreatedEvent) event);
        }
        // Add other event types here
    }
}
