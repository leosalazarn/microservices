package com.example.products.command;

import com.example.products.domain.aggregate.ProductAggregate;
import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.exception.ProductNotFoundException;
import com.example.products.infrastructure.eventstore.EventStore;
import com.example.products.infrastructure.mapper.ProductAggregateMapper;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.infrastructure.messaging.DomainEventPublisher;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, Product> {

    private final ProductRepository repository;
    private final EventStore eventStore;
    private final DomainEventPublisher domainEventPublisher;
    private final ProductMapper productMapper;
    private final ProductAggregateMapper aggregateMapper;

    @Override
    @Transactional
    public Product handle(UpdateProductCommand command) {
        ProductEntity entity = repository.findByIdAndActiveTrue(command.getId())
                .orElseThrow(() -> new ProductNotFoundException(command.getId()));

        ProductAggregate aggregate = aggregateMapper.toAggregate(entity);

        String oldName = aggregate.getName();
        Double oldPrice = aggregate.getPrice();
        String oldDescription = aggregate.getDescription();
        String oldCategory = aggregate.getCategory();

        if (command.getName() != null) {
            aggregate.updateName(command.getName());
        }
        if (command.getPrice() != null) {
            aggregate.updatePrice(command.getPrice());
        }
        if (command.getDescription() != null) {
            aggregate.updateDescription(command.getDescription());
        }
        if (command.getCategory() != null) {
            aggregate.updateCategory(command.getCategory());
        }

        aggregate.applyProductUpdated(oldName, oldPrice, oldDescription, oldCategory);

        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventStore.saveAll(events);
        domainEventPublisher.publishAll(events);
        aggregate.markEventsAsCommitted();

        ProductEntity savedEntity = repository.save(aggregateMapper.toEntity(aggregate));
        return productMapper.toModel(savedEntity);
    }
}
