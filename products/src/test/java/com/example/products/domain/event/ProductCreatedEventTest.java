package com.example.products.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductCreatedEventTest {

    @Test
    void of_ValidParameters_ShouldCreateEventCorrectly() {
        String productId = "test-id";
        String name = "Test Product";
        Double price = 100.0;
        Long version = 1L;

        ProductCreatedEvent event = ProductCreatedEvent.of(productId, name, price, version);

        assertNotNull(event);
        assertEquals(productId, event.getProductId());
        assertEquals(name, event.getName());
        assertEquals(price, event.getPrice());
        assertEquals(version, event.getVersion());
        assertNotNull(event.getOccurredAt());
        assertTrue(event.getOccurredAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void of_NullParameters_ShouldCreateEventWithNulls() {
        ProductCreatedEvent event = ProductCreatedEvent.of(null, null, null, null);

        assertNotNull(event);
        assertNull(event.getProductId());
        assertNull(event.getName());
        assertNull(event.getPrice());
        assertNull(event.getVersion());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void getAggregateId_ShouldReturnProductId() {
        String productId = "test-id";
        ProductCreatedEvent event = new ProductCreatedEvent();
        event.setProductId(productId);

        assertEquals(productId, event.getAggregateId());
    }

    @Test
    void getAggregateId_NullProductId_ShouldReturnNull() {
        ProductCreatedEvent event = new ProductCreatedEvent();
        event.setProductId(null);

        assertNull(event.getAggregateId());
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyEvent() {
        ProductCreatedEvent event = new ProductCreatedEvent();

        assertNotNull(event);
        assertNull(event.getProductId());
        assertNull(event.getName());
        assertNull(event.getPrice());
        assertNull(event.getVersion());
        assertNull(event.getOccurredAt());
    }

    @Test
    void constructor_AllArgs_ShouldCreateEventWithAllFields() {
        String productId = "test-id";
        String name = "Test Product";
        Double price = 100.0;
        LocalDateTime occurredAt = LocalDateTime.now();
        Long version = 1L;

        ProductCreatedEvent event = new ProductCreatedEvent(productId, name, price, occurredAt, version);

        assertEquals(productId, event.getProductId());
        assertEquals(name, event.getName());
        assertEquals(price, event.getPrice());
        assertEquals(occurredAt, event.getOccurredAt());
        assertEquals(version, event.getVersion());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        ProductCreatedEvent event = new ProductCreatedEvent();
        String productId = "test-id";
        String name = "Test Product";
        Double price = 100.0;
        LocalDateTime occurredAt = LocalDateTime.now();
        Long version = 1L;

        event.setProductId(productId);
        event.setName(name);
        event.setPrice(price);
        event.setOccurredAt(occurredAt);
        event.setVersion(version);

        assertEquals(productId, event.getProductId());
        assertEquals(name, event.getName());
        assertEquals(price, event.getPrice());
        assertEquals(occurredAt, event.getOccurredAt());
        assertEquals(version, event.getVersion());
    }
}
