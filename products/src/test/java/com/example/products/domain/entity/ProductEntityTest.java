package com.example.products.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityTest {

    private ProductEntity entity;

    @BeforeEach
    void setUp() {
        entity = new ProductEntity();
    }

    @Test
    void constructor_ShouldCreateEntityWithDefaults() {
        assertNotNull(entity);
        assertTrue(entity.getActive()); // Only active has default value
        // createdAt, updatedAt, version are null until set via factory method
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getVersion());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        String id = "test-id";
        String name = "Test Product";
        Double price = 100.0;
        String description = "Test Description";
        String category = "Test Category";
        Boolean active = false;
        LocalDateTime now = LocalDateTime.now();
        Long version = 1L;

        entity.setId(id);
        entity.setName(name);
        entity.setPrice(price);
        entity.setDescription(description);
        entity.setCategory(category);
        entity.setActive(active);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setVersion(version);

        assertEquals(id, entity.getId());
        assertEquals(name, entity.getName());
        assertEquals(price, entity.getPrice());
        assertEquals(description, entity.getDescription());
        assertEquals(category, entity.getCategory());
        assertEquals(active, entity.getActive());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals(version, entity.getVersion());
    }

    @Test
    void equals_SameId_ShouldReturnTrue() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setId("same-id");
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setId("same-id");

        assertEquals(entity1, entity2);
    }

    @Test
    void equals_DifferentId_ShouldReturnFalse() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setId("id-1");
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setId("id-2");

        assertNotEquals(entity1, entity2);
    }

    @Test
    void hashCode_SameId_ShouldReturnSameHashCode() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setId("same-id");
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setId("same-id");

        assertEquals(entity1.hashCode(), entity2.hashCode());
    }
}
