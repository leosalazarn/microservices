package com.example.products.query;

import com.example.products.domain.repository.ProductRepository;
import com.example.products.infrastructure.mapper.ProductMapper;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryHandler {
    
    private final ProductRepository repository;
    private final ProductMapper mapper;
    
    public List<Product> getAllProducts() {
        return repository.findByActiveTrue()
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }
}
