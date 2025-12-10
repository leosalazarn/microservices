package com.example.billing.domain;

import com.example.billing.enums.InvoiceStatus;
import lombok.Data;

@Data
public class Invoice {
    private Long id;
    private Long customerId;
    private Double amount;
    private InvoiceStatus status;
}
