package com.example.products.command;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.repository.ProductLookupRepository;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.eventstore.EventStore;
import com.example.products.infrastructure.mapper.ProductAggregateMapper;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.infrastructure.messaging.DomainEventPublisher;
import com.example.products.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductCommandHandlerTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductLookupRepository lookupRepository;

    @Mock
    private EventStore eventStore;

    @Mock
    private DomainEventPublisher domainEventPublisher;

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
        savedEntity.setId("507f1f77bcf86cd799439011");
        savedEntity.setVersion(1L);
        
        expectedProduct = new Product();
        expectedProduct.setId("507f1f77bcf86cd799439011");
        expectedProduct.setName("Test Product");
        expectedProduct.setPrice(100.0);
    }

    @Test
    void handle_ValidCommand_ShouldCreateProductSuccessfully() {
        when(lookupRepository.existsByName(anyString())).thenReturn(false);
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);
        when(productMapper.toModel(any(ProductEntity.class))).thenReturn(expectedProduct);

        Product result = handler.handle(command);

        assertNotNull(result);
        assertEquals(expectedProduct.getId(), result.getId());
        assertEquals(expectedProduct.getName(), result.getName());
        assertEquals(expectedProduct.getPrice(), result.getPrice());

        verify(lookupRepository).existsByName(command.getName());
        verify(repository).save(any(ProductEntity.class));
        verify(eventStore).saveAll(any());
        verify(domainEventPublisher).publishAll(any());
    }

    @Test
    void handle_NullName_ShouldThrowNullPointerException() {
        // Handler doesn't validate - validation happens in CommandBus
        // When called directly with null name, it will throw NPE in aggregate
        CreateProductCommand invalidCommand = new CreateProductCommand(null, 100.0, "Description", "Category");
        when(lookupRepository.existsByName(any())).thenReturn(false);

        assertThrows(NullPointerException.class, () -> handler.handle(invalidCommand));
    }

    @Test
    void handle_EmptyName_ShouldProcessSuccessfully() {
        // Handler doesn't validate empty strings - validation happens in CommandBus
        CreateProductCommand command = new CreateProductCommand("", 100.0, "Description", "Category");
        when(lookupRepository.existsByName(anyString())).thenReturn(false);
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);
        when(productMapper.toModel(any(ProductEntity.class))).thenReturn(expectedProduct);

        Product result = handler.handle(command);

        assertNotNull(result);
    }

    @Test
    void handle_NullPrice_ShouldProcessSuccessfully() {
        // Handler doesn't validate null price - validation happens in CommandBus
        CreateProductCommand command = new CreateProductCommand("Test Product", null, "Description", "Category");
        when(lookupRepository.existsByName(anyString())).thenReturn(false);
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);
        when(productMapper.toModel(any(ProductEntity.class))).thenReturn(expectedProduct);

        Product result = handler.handle(command);

        assertNotNull(result);
    }

    @Test
    void handle_NegativePrice_ShouldProcessSuccessfully() {
        // Handler doesn't validate negative price - validation happens in CommandBus
        CreateProductCommand command = new CreateProductCommand("Test Product", -10.0, "Description", "Category");
        when(lookupRepository.existsByName(anyString())).thenReturn(false);
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);
        when(productMapper.toModel(any(ProductEntity.class))).thenReturn(expectedProduct);

        Product result = handler.handle(command);

        assertNotNull(result);
    }

    @Test
    void handle_RepositoryException_ShouldPropagateException() {
        when(lookupRepository.existsByName(anyString())).thenReturn(false);
        when(aggregateMapper.toEntity(any())).thenReturn(new ProductEntity());
        when(repository.save(any(ProductEntity.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> handler.handle(command));

        verify(repository).save(any(ProductEntity.class));
        verify(eventStore, never()).saveAll(any());
        verify(domainEventPublisher, never()).publishAll(any());
    }
}
