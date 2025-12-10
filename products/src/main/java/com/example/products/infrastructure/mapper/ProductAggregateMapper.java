package com.example.products.infrastructure.mapper;

import com.example.products.domain.aggregate.ProductAggregate;
import com.example.products.domain.entity.ProductEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductAggregateMapper {
    
    public ProductEntity toEntity(ProductAggregate aggregate) {
        ProductEntity entity = new ProductEntity();
        BeanUtils.copyProperties(aggregate, entity);
        return entity;
    }
    
    public ProductAggregate toAggregate(ProductEntity entity) {
        ProductAggregate aggregate = new ProductAggregate();
        BeanUtils.copyProperties(entity, aggregate);
        return aggregate;
    }
}
