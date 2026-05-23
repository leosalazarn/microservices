package com.example.products.controller;

import com.example.products.api.ProductsCommandApi;
import com.example.products.command.CommandBus;
import com.example.products.command.CreateProductCommand;
import com.example.products.command.DeleteProductCommand;
import com.example.products.command.UpdateProductCommand;
import com.example.products.model.Product;
import com.example.products.model.ProductUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductCommandController implements ProductsCommandApi {
    
    private final CommandBus commandBus;
    private final CacheManager cacheManager;
    
    @Override
    public ResponseEntity<Product> createProduct(Product product) {
        CreateProductCommand command = new CreateProductCommand(
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getCategory()
        );
        
        Product createdProduct = commandBus.dispatch(command);
        return ResponseEntity.status(201).body(createdProduct);
    }
    
    @Override
    public ResponseEntity<Product> updateProduct(String id, ProductUpdate productUpdate) {
        UpdateProductCommand command = new UpdateProductCommand(
            id,
            productUpdate.getName(),
            productUpdate.getPrice(),
            productUpdate.getDescription(),
            productUpdate.getCategory()
        );
        
        Product updatedProduct = commandBus.dispatch(command);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @Override
    public ResponseEntity<String> evictCaches() {
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(cache -> {
                    log.info("Evicting cache: {}", cache.getName());
                    cache.clear();
                });
        return ResponseEntity.ok("All caches evicted");
    }

    @Override
    public ResponseEntity<Void> deleteProduct(String id) {
        commandBus.dispatch(new DeleteProductCommand(id));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
