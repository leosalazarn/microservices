package com.example.products.domain.aggregate;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductAggregate {

    private String id;
    private String name;
    private Double price;
    private String description;
    private String category;
    private Boolean active = true;  // Default value
    private LocalDateTime createdAt = LocalDateTime.now();  // Default value
    private LocalDateTime updatedAt = LocalDateTime.now();  // Default value
    private Long version = 0L;  // Default value
    
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // Business method: Update name
    public void updateName(String newName) {
        validateName(newName);
        
        if (this.name.equals(newName.trim())) {
            throw new IllegalArgumentException("New name must be different from current name");
        }
        
        this.name = newName.trim();
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Update price
    public void updatePrice(Double newPrice) {
        validatePrice(newPrice);
        
        if (this.price.equals(newPrice)) {
            throw new IllegalArgumentException("New price must be different from current price");
        }
        
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Apply discount
    public void applyDiscount(Double discountPercentage) {
        if (discountPercentage == null || discountPercentage <= 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        
        if (!this.active) {
            throw new IllegalStateException("Cannot apply discount to inactive product");
        }
        
        Double discountAmount = this.price * (discountPercentage / 100);
        this.price = this.price - discountAmount;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Update description
    public void updateDescription(String newDescription) {
        if (newDescription != null && newDescription.trim().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        
        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Update category
    public void updateCategory(String newCategory) {
        if (newCategory != null && newCategory.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        this.category = newCategory != null ? newCategory.trim() : null;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Deactivate product
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Product is already inactive");
        }
        
        this.active = false;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Activate product
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Product is already active");
        }
        
        this.active = true;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    // Business method: Check if product is available for sale
    public boolean isAvailableForSale() {
        return this.active && this.price != null && this.price > 0;
    }
    
    // Business method: Check if product needs restock
    public boolean needsRestock() {
        return this.active && this.price > 0;
    }
    
    // Apply product created event
    public void applyProductCreated() {
        if (this.id == null) {
            throw new IllegalStateException("Cannot apply ProductCreated event without ID");
        }
        
        ProductCreatedEvent event = ProductCreatedEvent.of(
            this.id,
            this.name,
            this.price,
            this.version
        );
        
        uncommittedEvents.add(event);
    }
    
    // Get uncommitted events for SAGA
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    // Clear events after publishing
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    // Validation methods
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        
        if (name.trim().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters");
        }
        
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
    }
    
    private static void validatePrice(Double price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        
        if (price <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        
        if (price > 1000000) {
            throw new IllegalArgumentException("Product price cannot exceed 1,000,000");
        }
    }
}
