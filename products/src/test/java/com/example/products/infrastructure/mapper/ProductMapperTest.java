package com.example.products.infrastructure.mapper;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper mapper;
    private ProductEntity entity;
    private Product model;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();

        entity = new ProductEntity();
        entity.setId("test-id");
        entity.setName("Test Product");
        entity.setPrice(100.0);
        entity.setDescription("Test Description");
        entity.setCategory("Test Category");
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVersion(1L);

        model = new Product();
        model.setId("507f1f77bcf86cd799439011");
        model.setName("Test Product");
        model.setPrice(100.0);
        model.setDescription("Test Description");
        model.setCategory("Test Category");
    }

    @Test
    void toModel_ValidEntity_ShouldMapCorrectly() {
        Product result = mapper.toModel(entity);

        assertNotNull(result);
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getPrice(), result.getPrice());
        assertEquals(entity.getDescription(), result.getDescription());
        assertEquals(entity.getCategory(), result.getCategory());
    }

    @Test
    void toModel_NullEntity_ShouldReturnNull() {
        Product result = mapper.toModel(null);
        assertNull(result);
    }

    @Test
    void toModel_EntityWithNullFields_ShouldHandleGracefully() {
        ProductEntity entityWithNulls = new ProductEntity();
        entityWithNulls.setId("test-id");
        entityWithNulls.setName("Test Product");
        entityWithNulls.setPrice(100.0);
        // description, category are null

        Product result = mapper.toModel(entityWithNulls);

        assertNotNull(result);
        assertEquals(entityWithNulls.getName(), result.getName());
        assertEquals(entityWithNulls.getPrice(), result.getPrice());
        assertNull(result.getDescription());
        assertNull(result.getCategory());
    }

    @Test
    void toEntity_ValidModel_ShouldMapCorrectly() {
        ProductEntity result = mapper.toEntity(model);

        assertNotNull(result);
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getPrice(), result.getPrice());
        assertEquals(model.getDescription(), result.getDescription());
        assertEquals(model.getCategory(), result.getCategory());
        // Default values should be set
        assertTrue(result.getActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(0L, result.getVersion());
    }

    @Test
    void toEntity_NullModel_ShouldReturnNull() {
        ProductEntity result = mapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_ModelWithNullFields_ShouldHandleGracefully() {
        Product modelWithNulls = new Product();
        modelWithNulls.setName("Test Product");
        modelWithNulls.setPrice(100.0);
        // id, description, category are null

        ProductEntity result = mapper.toEntity(modelWithNulls);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(modelWithNulls.getName(), result.getName());
        assertEquals(modelWithNulls.getPrice(), result.getPrice());
        assertNull(result.getDescription());
        assertNull(result.getCategory());
    }
}
