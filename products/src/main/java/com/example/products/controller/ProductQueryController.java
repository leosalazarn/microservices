package com.example.products.controller;

import com.example.products.api.ProductsQueryApi;
import com.example.products.model.Product;
import com.example.products.query.ProductQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductQueryController implements ProductsQueryApi {
    
    private final ProductQueryHandler queryHandler;
    
    @Override
    public ResponseEntity<String> getServiceInfo() {
        return ResponseEntity.ok("Hello from Products Service");
    }
    
    @Override
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("OK");
    }
    
    @Override
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = queryHandler.getAllProducts();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(products.size()))
                .body(products);
    }
}
