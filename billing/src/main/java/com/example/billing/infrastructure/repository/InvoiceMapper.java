package com.example.billing.infrastructure.repository;

import com.example.billing.model.Invoice;
import com.example.billing.model.Invoice.StatusEnum;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public Invoice toModel(InvoiceEntity entity) {
        Invoice model = new Invoice();
        model.setId(entity.getId());
        model.setCustomerId(entity.getCustomerId());
        model.setAmount(entity.getAmount());
        model.setStatus(StatusEnum.fromValue(entity.getStatus()));
        return model;
    }

    public InvoiceEntity toEntity(Invoice model) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(model.getId());
        entity.setCustomerId(model.getCustomerId());
        entity.setAmount(model.getAmount());
        entity.setStatus(model.getStatus().getValue());
        return entity;
    }

    public void updateEntity(InvoiceEntity entity, Invoice model) {
        if (model.getCustomerId() != null) {
            entity.setCustomerId(model.getCustomerId());
        }
        if (model.getAmount() != null) {
            entity.setAmount(model.getAmount());
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus().getValue());
        }
        entity.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
