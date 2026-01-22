package com.example.products.query;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductQueryHandlerTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductQueryHandler queryHandler;

    private List<ProductEntity> mockEntities;
    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setId("1");
        entity1.setName("Product 1");
        entity1.setPrice(100.0);

        ProductEntity entity2 = new ProductEntity();
        entity2.setId("2");
        entity2.setName("Product 2");
        entity2.setPrice(200.0);

        mockEntities = Arrays.asList(entity1, entity2);

        Product product1 = new Product();
        product1.setId("507f1f77bcf86cd799439011");
        product1.setName("Product 1");
        product1.setPrice(100.0);

        Product product2 = new Product();
        product2.setId("507f1f77bcf86cd799439012");
        product2.setName("Product 2");
        product2.setPrice(200.0);

        mockProducts = Arrays.asList(product1, product2);
    }

    @Test
    void getAllProducts_ShouldReturnMappedProducts() {
        when(repository.findByActiveTrue()).thenReturn(mockEntities);
        when(mapper.toModel(mockEntities.get(0))).thenReturn(mockProducts.get(0));
        when(mapper.toModel(mockEntities.get(1))).thenReturn(mockProducts.get(1));

        List<Product> result = queryHandler.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());

        verify(repository).findByActiveTrue();
        verify(mapper, times(2)).toModel(any(ProductEntity.class));
    }

    @Test
    void getAllProducts_EmptyRepository_ShouldReturnEmptyList() {
        when(repository.findByActiveTrue()).thenReturn(Arrays.asList());

        List<Product> result = queryHandler.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository).findByActiveTrue();
        verify(mapper, never()).toModel(any(ProductEntity.class));
    }

    @Test
    void getAllProducts_RepositoryThrowsException_ShouldPropagateException() {
        when(repository.findByActiveTrue()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> queryHandler.getAllProducts());

        verify(repository).findByActiveTrue();
        verify(mapper, never()).toModel(any(ProductEntity.class));
    }
}
