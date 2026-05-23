package com.example.products.infrastructure.eventstore;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.event.ProductUpdatedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventTypeRegistry {

    private final Map<String, Class<? extends DomainEvent>> registry = new ConcurrentHashMap<>();

    public EventTypeRegistry() {
        register(ProductCreatedEvent.class);
        register(ProductUpdatedEvent.class);
    }

    public void register(Class<? extends DomainEvent> eventClass) {
        registry.put(eventClass.getSimpleName(), eventClass);
        registry.put(eventClass.getName(), eventClass);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends DomainEvent> lookup(String typeName) {
        Class<? extends DomainEvent> eventClass = registry.get(typeName);
        if (eventClass == null) {
            try {
                eventClass = (Class<? extends DomainEvent>) Class.forName(typeName);
                registry.put(typeName, eventClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unknown event type: " + typeName, e);
            }
        }
        return eventClass;
    }

    public String resolve(Class<? extends DomainEvent> eventClass) {
        return eventClass.getSimpleName();
    }
}
