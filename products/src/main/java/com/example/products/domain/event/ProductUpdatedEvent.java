package com.example.products.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdatedEvent implements DomainEvent {
    private String productId;
    private String oldName;
    private String newName;
    private Double oldPrice;
    private Double newPrice;
    private String oldDescription;
    private String newDescription;
    private String oldCategory;
    private String newCategory;
    private LocalDateTime occurredAt;
    private Long version;

    public static ProductUpdatedEvent of(String productId, String oldName, String newName,
                                          Double oldPrice, Double newPrice,
                                          String oldDescription, String newDescription,
                                          String oldCategory, String newCategory,
                                          Long version) {
        ProductUpdatedEvent event = new ProductUpdatedEvent();
        event.setProductId(productId);
        event.setOldName(oldName);
        event.setNewName(newName);
        event.setOldPrice(oldPrice);
        event.setNewPrice(newPrice);
        event.setOldDescription(oldDescription);
        event.setNewDescription(newDescription);
        event.setOldCategory(oldCategory);
        event.setNewCategory(newCategory);
        event.setOccurredAt(LocalDateTime.now());
        event.setVersion(version);
        return event;
    }

    @Override
    public String getAggregateId() {
        return productId;
    }
}
