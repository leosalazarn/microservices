package com.example.products.command;

import com.example.products.domain.aggregate.ProductAggregate;
import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.eventstore.EventStore;
import com.example.products.infrastructure.mapper.ProductAggregateMapper;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.infrastructure.messaging.DomainEventPublisher;
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
    private final DomainEventPublisher domainEventPublisher;
    private final ProductMapper productMapper;
    private final ProductAggregateMapper aggregateMapper;
    
    @Override
    @Transactional
    public Product handle(CreateProductCommand command) {
        // Create and process aggregate
        ProductAggregate aggregate = createAggregate(command);
        
        // Persist entity
        ProductEntity savedEntity = persistEntity(aggregate);
        
        // Update aggregate with generated data
        updateAggregateFromEntity(aggregate, savedEntity);
        
        // Process domain events
        processDomainEvents(aggregate);
        
        return productMapper.toModel(savedEntity);
    }
    
    private ProductAggregate createAggregate(CreateProductCommand command) {
        ProductAggregate aggregate = new ProductAggregate();
        BeanUtils.copyProperties(command, aggregate);
        aggregate.applyProductCreated();
        return aggregate;
    }
    
    private ProductEntity persistEntity(ProductAggregate aggregate) {
        ProductEntity entity = aggregateMapper.toEntity(aggregate);
        return repository.save(entity);
    }
    
    private void updateAggregateFromEntity(ProductAggregate aggregate, ProductEntity entity) {
        aggregate.setId(entity.getId());
        aggregate.setVersion(entity.getVersion());
    }
    
    private void processDomainEvents(ProductAggregate aggregate) {
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        
        // Save to event store
        eventStore.saveAll(events);
        
        // Publish events for SAGA
        domainEventPublisher.publishAll(events);
        
        // Mark events as committed
        aggregate.markEventsAsCommitted();
    }
}
