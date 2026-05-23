package com.example.billing.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEvent {
    private String productId;
    private String name;
    private Double price;
    private LocalDateTime occurredAt;
}
