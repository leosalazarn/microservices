package com.example.products.infrastructure.mapper;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    
    public Product toModel(ProductEntity entity) {
        if (entity == null) return null;
        
        Product product = new Product();
        product.setId(generateLongId(entity.getId()));
        product.setName(entity.getName());
        product.setPrice(entity.getPrice());
        product.setDescription(entity.getDescription());
        product.setCategory(entity.getCategory());
        return product;
    }
    
    public ProductEntity toEntity(Product model) {
        if (model == null) return null;
        
        // Use factory method instead of direct instantiation (DDD)
        return ProductEntity.create(
            model.getName(),
            model.getPrice(),
            model.getDescription(),
            model.getCategory()
        );
    }
    
    // DRY: Centralized ID conversion logic
    private Long generateLongId(String mongoId) {
        if (mongoId == null || mongoId.isEmpty()) {
            return null;
        }
        return (long) Math.abs(mongoId.hashCode());
    }
}
