package com.example.products.domain.repository;

import com.example.products.domain.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    
    // Query methods for CQRS read operations
    List<ProductEntity> findByActiveTrue();
    
    List<ProductEntity> findByCategory(String category);
    
    List<ProductEntity> findByActiveTrueAndCategory(String category);
    
    Page<ProductEntity> findByActiveTrue(Pageable pageable);
    
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    
    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List<ProductEntity> findByPriceBetween(Double minPrice, Double maxPrice);
    
    @Query("{ 'active': true, 'price': { $gte: ?0, $lte: ?1 } }")
    List<ProductEntity> findActiveProductsByPriceRange(Double minPrice, Double maxPrice);
    
    Optional<ProductEntity> findByIdAndActiveTrue(String id);
    
    boolean existsByNameAndActiveTrue(String name);
}
