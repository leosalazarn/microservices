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
public class ProductUpdatedEvent implements DomainEvent {
    String productId;
    String oldName;
    String newName;
    Double oldPrice;
    Double newPrice;
    String oldDescription;
    String newDescription;
    String oldCategory;
    String newCategory;
    LocalDateTime occurredAt;
    Long version;

    public static ProductUpdatedEvent of(String productId, String oldName, String newName,
                                         Double oldPrice, Double newPrice,
                                         String oldDescription, String newDescription,
                                         String oldCategory, String newCategory,
                                         Long version) {
        return new ProductUpdatedEvent(productId, oldName, newName, oldPrice, newPrice,
                oldDescription, newDescription, oldCategory, newCategory,
                LocalDateTime.now(), version);
    }

    @Override
    public String getAggregateId() {
        return productId;
    }
}
