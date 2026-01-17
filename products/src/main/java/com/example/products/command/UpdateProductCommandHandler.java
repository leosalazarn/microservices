package com.example.products.command;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.exception.ProductNotFoundException;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, Product> {
    
    private final ProductRepository repository;
    private final ProductMapper productMapper;
    
    @Override
    @Transactional
    public Product handle(UpdateProductCommand command) {
        // Set-Based Consistency Validation: product must exist
        ProductEntity entity = repository.findByIdAndActiveTrue(command.getId())
                .orElseThrow(() -> new ProductNotFoundException(command.getId()));
        
        // Apply updates (only non-null fields)
        if (command.getName() != null) {
            entity.setName(command.getName());
        }
        if (command.getPrice() != null) {
            entity.setPrice(command.getPrice());
        }
        if (command.getDescription() != null) {
            entity.setDescription(command.getDescription());
        }
        if (command.getCategory() != null) {
            entity.setCategory(command.getCategory());
        }
        
        ProductEntity savedEntity = repository.save(entity);
        return productMapper.toModel(savedEntity);
    }
}
