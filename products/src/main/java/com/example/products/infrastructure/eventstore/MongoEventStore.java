package com.example.products.infrastructure.eventstore;

import com.example.products.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoEventStore implements EventStore {
    
    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;
    
    @Override
    public void save(DomainEvent event) {
        EventStoreEntity entity = new EventStoreEntity();
        entity.setAggregateId(event.getAggregateId());
        entity.setEventType(event.getClass().getName());
        entity.setEventData(serializeEvent(event));
        entity.setVersion(event.getVersion());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setStoredAt(LocalDateTime.now());
        
        repository.save(entity);
    }
    
    @Override
    public void saveAll(List<DomainEvent> events) {
        events.forEach(this::save);
    }
    
    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return repository.findByAggregateIdOrderByVersionAsc(aggregateId)
                .stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DomainEvent> getAllEvents() {
        return repository.findAll()
                .stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
    }
    
    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("Failed to serialize event: {} (aggregate: {})", event.getClass().getSimpleName(), event.getAggregateId(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
    
    private DomainEvent deserializeEvent(EventStoreEntity entity) {
        try {
            Class<?> eventClass = Class.forName(entity.getEventType());
            return (DomainEvent) objectMapper.readValue(entity.getEventData(), eventClass);
        } catch (Exception e) {
            log.error("Failed to deserialize event type: {}", entity.getEventType(), e);
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}
