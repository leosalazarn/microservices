package com.example.products.controller;

import com.example.products.model.Product;
import com.example.products.query.ProductQueryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductQueryControllerTest {

    @Mock
    private ProductQueryHandler queryHandler;

    @InjectMocks
    private ProductQueryController controller;

    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(100.0);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(200.0);

        mockProducts = Arrays.asList(product1, product2);
    }

    @Test
    void getAllProducts_ShouldReturnProductList() {
        when(queryHandler.getAllProducts()).thenReturn(mockProducts);

        ResponseEntity<List<Product>> response = controller.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Product 1", response.getBody().get(0).getName());
        assertEquals("Product 2", response.getBody().get(1).getName());

        verify(queryHandler).getAllProducts();
    }

    @Test
    void getAllProducts_EmptyList_ShouldReturnEmptyList() {
        when(queryHandler.getAllProducts()).thenReturn(Arrays.asList());

        ResponseEntity<List<Product>> response = controller.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(queryHandler).getAllProducts();
    }

    @Test
    void getAllProducts_QueryHandlerThrowsException_ShouldPropagateException() {
        when(queryHandler.getAllProducts()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> controller.getAllProducts());

        verify(queryHandler).getAllProducts();
    }
}
