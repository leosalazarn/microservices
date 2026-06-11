package com.example.products.streams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEvent {
    String productId;
    String name;
    Double price;
    String description;
    String category;
    Double oldPrice;
    Double newPrice;
    String oldCategory;
    String newCategory;
    LocalDateTime occurredAt;
    Long version;
}
