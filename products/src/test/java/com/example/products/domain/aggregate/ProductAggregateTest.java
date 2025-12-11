package com.example.products.domain.aggregate;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductAggregateTest {

    private ProductAggregate aggregate;

    @BeforeEach
    void setUp() {
        aggregate = new ProductAggregate();
        aggregate.setId("test-id");
        aggregate.setName("Test Product");
        aggregate.setPrice(100.0);
    }

    @Test
    void updateName_ValidName_ShouldUpdateSuccessfully() {
        String newName = "Updated Product";
        LocalDateTime beforeUpdate = aggregate.getUpdatedAt();
        Long beforeVersion = aggregate.getVersion();

        aggregate.updateName(newName);

        assertEquals(newName, aggregate.getName());
        assertTrue(aggregate.getUpdatedAt().isAfter(beforeUpdate));
        assertEquals(beforeVersion + 1, aggregate.getVersion());
    }

    @Test
    void updateName_SameName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateName("Test Product"));
    }

    @Test
    void updateName_NullName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateName(null));
    }

    @Test
    void updateName_EmptyName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateName(""));
    }

    @Test
    void updateName_ShortName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateName("AB"));
    }

    @Test
    void updatePrice_ValidPrice_ShouldUpdateSuccessfully() {
        Double newPrice = 150.0;
        LocalDateTime beforeUpdate = aggregate.getUpdatedAt();
        Long beforeVersion = aggregate.getVersion();

        aggregate.updatePrice(newPrice);

        assertEquals(newPrice, aggregate.getPrice());
        assertTrue(aggregate.getUpdatedAt().isAfter(beforeUpdate));
        assertEquals(beforeVersion + 1, aggregate.getVersion());
    }

    @Test
    void updatePrice_SamePrice_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updatePrice(100.0));
    }

    @Test
    void updatePrice_NullPrice_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updatePrice(null));
    }

    @Test
    void updatePrice_NegativePrice_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updatePrice(-10.0));
    }

    @Test
    void applyDiscount_ValidDiscount_ShouldApplySuccessfully() {
        Double originalPrice = aggregate.getPrice();
        Double discountPercentage = 10.0;
        Double expectedPrice = originalPrice * 0.9;

        aggregate.applyDiscount(discountPercentage);

        assertEquals(expectedPrice, aggregate.getPrice());
    }

    @Test
    void applyDiscount_InvalidPercentage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.applyDiscount(null));
        assertThrows(IllegalArgumentException.class, () -> aggregate.applyDiscount(-5.0));
        assertThrows(IllegalArgumentException.class, () -> aggregate.applyDiscount(150.0));
    }

    @Test
    void applyDiscount_InactiveProduct_ShouldThrowException() {
        aggregate.setActive(false);
        assertThrows(IllegalStateException.class, () -> aggregate.applyDiscount(10.0));
    }

    @Test
    void deactivate_ActiveProduct_ShouldDeactivateSuccessfully() {
        assertTrue(aggregate.getActive());

        aggregate.deactivate();

        assertFalse(aggregate.getActive());
    }

    @Test
    void deactivate_InactiveProduct_ShouldThrowException() {
        aggregate.setActive(false);
        assertThrows(IllegalStateException.class, () -> aggregate.deactivate());
    }

    @Test
    void activate_InactiveProduct_ShouldActivateSuccessfully() {
        aggregate.setActive(false);

        aggregate.activate();

        assertTrue(aggregate.getActive());
    }

    @Test
    void activate_ActiveProduct_ShouldThrowException() {
        assertThrows(IllegalStateException.class, () -> aggregate.activate());
    }

    @Test
    void isAvailableForSale_ActiveWithValidPrice_ShouldReturnTrue() {
        assertTrue(aggregate.isAvailableForSale());
    }

    @Test
    void isAvailableForSale_InactiveProduct_ShouldReturnFalse() {
        aggregate.setActive(false);
        assertFalse(aggregate.isAvailableForSale());
    }

    @Test
    void isAvailableForSale_ZeroPrice_ShouldReturnFalse() {
        aggregate.setPrice(0.0);
        assertFalse(aggregate.isAvailableForSale());
    }

    @Test
    void applyProductCreated_WithId_ShouldAddEvent() {
        aggregate.applyProductCreated();

        List<DomainEvent> events = aggregate.getUncommittedEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductCreatedEvent);
    }

    @Test
    void applyProductCreated_WithoutId_ShouldThrowException() {
        aggregate.setId(null);
        assertThrows(IllegalStateException.class, () -> aggregate.applyProductCreated());
    }

    @Test
    void markEventsAsCommitted_ShouldClearEvents() {
        aggregate.applyProductCreated();
        assertEquals(1, aggregate.getUncommittedEvents().size());

        aggregate.markEventsAsCommitted();

        assertEquals(0, aggregate.getUncommittedEvents().size());
    }

    @Test
    void updateDescription_ValidDescription_ShouldUpdateSuccessfully() {
        String description = "Test description";
        aggregate.updateDescription(description);

        assertEquals(description, aggregate.getDescription());
    }

    @Test
    void updateDescription_TooLong_ShouldThrowException() {
        String longDescription = "A".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateDescription(longDescription));
    }

    @Test
    void updateCategory_ValidCategory_ShouldUpdateSuccessfully() {
        String category = "Electronics";
        aggregate.updateCategory(category);

        assertEquals(category, aggregate.getCategory());
    }

    @Test
    void updateCategory_EmptyCategory_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> aggregate.updateCategory(""));
    }
}
