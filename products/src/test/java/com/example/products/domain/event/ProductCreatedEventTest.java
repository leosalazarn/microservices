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
        String description = "A test product";
        String category = "electronics";
        Long version = 1L;

        ProductCreatedEvent event = ProductCreatedEvent.of(productId, name, price, description, category, version);

        assertNotNull(event);
        assertEquals(productId, event.getProductId());
        assertEquals(name, event.getName());
        assertEquals(price, event.getPrice());
        assertEquals(description, event.getDescription());
        assertEquals(category, event.getCategory());
        assertEquals(version, event.getVersion());
        assertNotNull(event.getOccurredAt());
        assertTrue(event.getOccurredAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void of_NullParameters_ShouldCreateEventWithNulls() {
        ProductCreatedEvent event = ProductCreatedEvent.of(null, null, null, null, null, null);

        assertNotNull(event);
        assertNull(event.getProductId());
        assertNull(event.getName());
        assertNull(event.getPrice());
        assertNull(event.getDescription());
        assertNull(event.getCategory());
        assertNull(event.getVersion());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void getAggregateId_ShouldReturnProductId() {
        ProductCreatedEvent event = ProductCreatedEvent.of("test-id", null, null, null, null, 1L);

        assertEquals("test-id", event.getAggregateId());
    }

    @Test
    void getAggregateId_NullProductId_ShouldReturnNull() {
        ProductCreatedEvent event = ProductCreatedEvent.of(null, null, null, null, null, null);

        assertNull(event.getAggregateId());
    }

    @Test
    void allArgsConstructor_ShouldCreateEventWithAllFields() {
        String productId = "test-id";
        String name = "Test Product";
        Double price = 100.0;
        String description = "A test product";
        String category = "electronics";
        LocalDateTime occurredAt = LocalDateTime.now();
        Long version = 1L;

        ProductCreatedEvent event = new ProductCreatedEvent(productId, name, price, description, category, occurredAt, version);

        assertEquals(productId, event.getProductId());
        assertEquals(name, event.getName());
        assertEquals(price, event.getPrice());
        assertEquals(description, event.getDescription());
        assertEquals(category, event.getCategory());
        assertEquals(occurredAt, event.getOccurredAt());
        assertEquals(version, event.getVersion());
    }
}
