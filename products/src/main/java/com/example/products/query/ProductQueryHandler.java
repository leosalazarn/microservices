package com.example.products.query;

import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryHandler {
    
    private final ProductRepository repository;
    private final ProductMapper mapper;
    
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        log.info("Fetching products from database (cache miss)");
        return repository.findByActiveTrue()
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }
}
