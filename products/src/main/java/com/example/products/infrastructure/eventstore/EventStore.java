package com.example.products.infrastructure.eventstore;

import com.example.products.domain.event.DomainEvent;

import java.util.List;

public interface EventStore {
    void save(DomainEvent event);
    void saveAll(List<DomainEvent> events);
    List<DomainEvent> getEvents(String aggregateId);
    List<DomainEvent> getAllEvents();
}
