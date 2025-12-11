package com.example.products.infrastructure.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreEntityTest {

    private EventStoreEntity entity;

    @BeforeEach
    void setUp() {
        entity = new EventStoreEntity();
    }

    @Test
    void constructor_ShouldCreateEntity() {
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getAggregateId());
        assertNull(entity.getEventType());
        assertNull(entity.getEventData());
        assertNull(entity.getOccurredAt());
        assertNull(entity.getVersion());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        String id = "event-id";
        String aggregateId = "aggregate-id";
        String eventType = "ProductCreatedEvent";
        String eventData = "{\"productId\":\"test-id\"}";
        LocalDateTime occurredAt = LocalDateTime.now();
        Long version = 1L;

        entity.setId(id);
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setEventData(eventData);
        entity.setOccurredAt(occurredAt);
        entity.setVersion(version);

        assertEquals(id, entity.getId());
        assertEquals(aggregateId, entity.getAggregateId());
        assertEquals(eventType, entity.getEventType());
        assertEquals(eventData, entity.getEventData());
        assertEquals(occurredAt, entity.getOccurredAt());
        assertEquals(version, entity.getVersion());
    }

    @Test
    void toString_ShouldContainAllFields() {
        entity.setId("event-id");
        entity.setAggregateId("aggregate-id");
        entity.setEventType("ProductCreatedEvent");

        String toString = entity.toString();

        assertTrue(toString.contains("event-id"));
        assertTrue(toString.contains("aggregate-id"));
        assertTrue(toString.contains("ProductCreatedEvent"));
    }
}
