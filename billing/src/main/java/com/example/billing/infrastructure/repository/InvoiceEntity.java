package com.example.billing.infrastructure.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "invoices")
public class InvoiceEntity {

    @Id
    private String id;

    @Field("customer_id")
    private Long customerId;

    @Field("amount")
    private Double amount;

    @Field("status")
    private String status;

    @Field("product_name")
    private String productName;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    public static InvoiceEntity create(Long customerId, Double amount, String productName) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.customerId = customerId;
        entity.amount = amount;
        entity.status = "PENDING";
        entity.productName = productName;
        LocalDateTime now = LocalDateTime.now();
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void markPaid() {
        this.status = "PAID";
        this.updatedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
    }
}
