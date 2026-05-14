package com.example.billing.command;

import com.example.billing.domain.entity.InvoiceEntity;
import com.example.billing.domain.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceCommandHandler {

    private final InvoiceRepository repository;

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(System.currentTimeMillis());
        entity.setCustomerId(invoice.getCustomerId());
        entity.setAmount(invoice.getAmount());
        entity.setStatus(invoice.getStatus().getValue());
        entity.setCreatedAt(LocalDateTime.now());

        InvoiceEntity saved = repository.save(entity);

        invoice.setId(saved.getId());
        invoice.setStatus(Invoice.StatusEnum.fromValue(saved.getStatus()));
        return invoice;
    }
}
