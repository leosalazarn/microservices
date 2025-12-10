package com.example.products.command;

import com.example.products.domain.aggregate.ProductAggregate;
import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.eventstore.EventStore;
import com.example.products.infrastructure.mapper.ProductAggregateMapper;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.infrastructure.messaging.EventPublisher;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand, Product> {
    
    private final ProductRepository repository;
    private final EventStore eventStore;
    private final EventPublisher eventPublisher;
    private final ProductMapper productMapper;
    private final ProductAggregateMapper aggregateMapper;
    
    @Override
    @Transactional
    public Product handle(CreateProductCommand command) {
        // Validate
        validateCommand(command);
        
        // Create aggregate using BeanUtils (default values set in aggregate)
        ProductAggregate aggregate = new ProductAggregate();
        BeanUtils.copyProperties(command, aggregate);
        
        // Convert to entity and save
        ProductEntity entity = aggregateMapper.toEntity(aggregate);
        ProductEntity savedEntity = repository.save(entity);
        
        // Update aggregate with generated ID and version
        aggregate.setId(savedEntity.getId());
        aggregate.setVersion(savedEntity.getVersion());
        
        // Apply domain event
        aggregate.applyProductCreated();
        
        // Get uncommitted events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        
        // Save to event store
        eventStore.saveAll(events);
        
        // Publish to Kafka for SAGA
        events.forEach(event -> {
            if (event instanceof ProductCreatedEvent) {
                eventPublisher.publishProductCreatedEvent((ProductCreatedEvent) event);
            }
        });
        
        // Mark events as committed
        aggregate.markEventsAsCommitted();
        
        return productMapper.toModel(savedEntity);
    }
    
    private void validateCommand(CreateProductCommand command) {
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (command.getPrice() == null || command.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
    }
}
