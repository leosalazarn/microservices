package com.example.products.domain.repository;

import com.example.products.domain.entity.ProductLookupEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLookupRepository extends MongoRepository<ProductLookupEntity, String> {

    boolean existsByName(String name);
}
