package com.example.products.controller;

import com.example.products.api.ProductsCommandApi;
import com.example.products.command.CommandBus;
import com.example.products.command.CreateProductCommand;
import com.example.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductCommandController implements ProductsCommandApi {
    
    private final CommandBus commandBus;
    
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
}
