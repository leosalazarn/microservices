package com.example.products.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent implements DomainEvent {
    private String productId;
    private String name;
    private Double price;
    private LocalDateTime occurredAt;
    private Long version;
    
    public static ProductCreatedEvent of(String productId, String name, Double price, Long version) {
        ProductCreatedEvent event = new ProductCreatedEvent();
        event.setProductId(productId);
        event.setName(name);
        event.setPrice(price);
        event.setOccurredAt(LocalDateTime.now());
        event.setVersion(version);
        return event;
    }
    
    @Override
    public String getAggregateId() {
        return productId;
    }
}
