package com.example.products.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCreatedEvent implements DomainEvent {
    String productId;
    String name;
    Double price;
    String description;
    String category;
    LocalDateTime occurredAt;
    Long version;

    public static ProductCreatedEvent of(String productId, String name, Double price,
                                         String description, String category, Long version) {
        return new ProductCreatedEvent(productId, name, price, description, category,
                LocalDateTime.now(), version);
    }

    @Override
    public String getAggregateId() {
        return productId;
    }
}
