package com.example.billing.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "invoices")
public class InvoiceEntity {

    @Id
    private Long id;

    @Field("customer_id")
    private Long customerId;

    @Field("amount")
    private Double amount;

    @Field("status")
    private String status;

    @Field("created_at")
    private LocalDateTime createdAt;
}
