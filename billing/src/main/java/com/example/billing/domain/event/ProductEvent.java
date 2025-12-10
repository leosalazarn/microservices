package com.example.billing.domain.event;

import lombok.Data;

@Data
public class ProductEvent {
    private String id;
    private String name;
    private Double price;
    private String createdAt;
    private String updatedAt;
}
