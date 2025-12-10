package com.example.products.domain.entity;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class ProductEntity {
    
    @Id
    private String id;
    
    @Field("name")
    @Indexed
    private String name;
    
    @Field("price")
    private Double price;
    
    @Field("description")
    private String description;
    
    @Field("category")
    @Indexed
    private String category;
    
    @Field("active")
    private Boolean active = true;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("version")
    private Long version;
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    public static ProductEntity create(String name, Double price, String description, String category) {
        ProductEntity product = new ProductEntity();
        product.name = name;
        product.price = price;
        product.description = description;
        product.category = category;
        product.active = true;
        
        LocalDateTime now = LocalDateTime.now();
        product.createdAt = now;
        product.updatedAt = now;
        product.version = 0L;
        
        return product;
    }
    
    public void updatePrice(Double newPrice) {
        if (newPrice == null || newPrice <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void raiseProductCreatedEvent() {
        ProductCreatedEvent event = ProductCreatedEvent.of(this.id, this.name, this.price, this.version);
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
