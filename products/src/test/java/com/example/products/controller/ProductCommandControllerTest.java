package com.example.products.controller;

import com.example.products.command.CommandBus;
import com.example.products.command.CreateProductCommand;
import com.example.products.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCommandControllerTest {

    @Mock
    private CommandBus commandBus;

    @InjectMocks
    private ProductCommandController controller;

    private Product inputProduct;
    private Product createdProduct;

    @BeforeEach
    void setUp() {
        inputProduct = new Product();
        inputProduct.setName("Test Product");
        inputProduct.setPrice(100.0);
        inputProduct.setDescription("Test Description");
        inputProduct.setCategory("Test Category");

        createdProduct = new Product();
        createdProduct.setId(1L);
        createdProduct.setName("Test Product");
        createdProduct.setPrice(100.0);
        createdProduct.setDescription("Test Description");
        createdProduct.setCategory("Test Category");
    }

    @Test
    void createProduct_ValidProduct_ShouldReturnCreatedProduct() {
        when(commandBus.dispatch(any(CreateProductCommand.class))).thenReturn(createdProduct);

        ResponseEntity<Product> response = controller.createProduct(inputProduct);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdProduct.getId(), response.getBody().getId());
        assertEquals(createdProduct.getName(), response.getBody().getName());
        assertEquals(createdProduct.getPrice(), response.getBody().getPrice());

        verify(commandBus).dispatch(any(CreateProductCommand.class));
    }

    @Test
    void createProduct_CommandBusThrowsException_ShouldPropagateException() {
        when(commandBus.dispatch(any(CreateProductCommand.class)))
                .thenThrow(new IllegalArgumentException("Invalid product data"));

        assertThrows(IllegalArgumentException.class, () -> controller.createProduct(inputProduct));

        verify(commandBus).dispatch(any(CreateProductCommand.class));
    }

    @Test
    void createProduct_NullProduct_ShouldHandleGracefully() {
        Product nullProduct = new Product();
        
        when(commandBus.dispatch(any(CreateProductCommand.class))).thenReturn(createdProduct);

        ResponseEntity<Product> response = controller.createProduct(nullProduct);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(commandBus).dispatch(any(CreateProductCommand.class));
    }
}
