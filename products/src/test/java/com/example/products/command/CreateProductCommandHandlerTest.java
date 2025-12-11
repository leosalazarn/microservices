package com.example.products.command;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.eventstore.EventStore;
import com.example.products.infrastructure.mapper.ProductAggregateMapper;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.infrastructure.messaging.EventPublisher;
import com.example.products.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductCommandHandlerTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private EventStore eventStore;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductAggregateMapper aggregateMapper;

    @InjectMocks
    private CreateProductCommandHandler handler;

    private CreateProductCommand command;
    private ProductEntity savedEntity;
    private Product expectedProduct;

    @BeforeEach
    void setUp() {
        command = new CreateProductCommand("Test Product", 100.0, "Description", "Category");
        
        savedEntity = new ProductEntity();
        savedEntity.setId("generated-id");
        savedEntity.setVersion(1L);
        
        expectedProduct = new Product();
        expectedProduct.setId(1L);
        expectedProduct.setName("Test Product");
        expectedProduct.setPrice(100.0);
    }

    @Test
    void handle_ValidCommand_ShouldCreateProductSuccessfully() {
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);
        when(productMapper.toModel(any(ProductEntity.class))).thenReturn(expectedProduct);

        Product result = handler.handle(command);

        assertNotNull(result);
        assertEquals(expectedProduct.getId(), result.getId());
        assertEquals(expectedProduct.getName(), result.getName());
        assertEquals(expectedProduct.getPrice(), result.getPrice());

        verify(repository).save(any(ProductEntity.class));
        verify(eventStore).saveAll(any());
        verify(eventPublisher).publishProductCreatedEvent(any(ProductCreatedEvent.class));
    }

    @Test
    void handle_NullName_ShouldThrowException() {
        CreateProductCommand invalidCommand = new CreateProductCommand(null, 100.0, "Description", "Category");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(invalidCommand));

        verify(repository, never()).save(any());
        verify(eventStore, never()).saveAll(any());
        verify(eventPublisher, never()).publishProductCreatedEvent(any());
    }

    @Test
    void handle_EmptyName_ShouldThrowException() {
        CreateProductCommand invalidCommand = new CreateProductCommand("", 100.0, "Description", "Category");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(invalidCommand));

        verify(repository, never()).save(any());
        verify(eventStore, never()).saveAll(any());
        verify(eventPublisher, never()).publishProductCreatedEvent(any());
    }

    @Test
    void handle_NullPrice_ShouldThrowException() {
        CreateProductCommand invalidCommand = new CreateProductCommand("Test Product", null, "Description", "Category");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(invalidCommand));

        verify(repository, never()).save(any());
        verify(eventStore, never()).saveAll(any());
        verify(eventPublisher, never()).publishProductCreatedEvent(any());
    }

    @Test
    void handle_NegativePrice_ShouldThrowException() {
        CreateProductCommand invalidCommand = new CreateProductCommand("Test Product", -10.0, "Description", "Category");

        assertThrows(IllegalArgumentException.class, () -> handler.handle(invalidCommand));

        verify(repository, never()).save(any());
        verify(eventStore, never()).saveAll(any());
        verify(eventPublisher, never()).publishProductCreatedEvent(any());
    }

    @Test
    void handle_RepositoryException_ShouldPropagateException() {
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> handler.handle(command));

        verify(repository).save(any(ProductEntity.class));
        verify(eventStore, never()).saveAll(any());
        verify(eventPublisher, never()).publishProductCreatedEvent(any());
    }
}
