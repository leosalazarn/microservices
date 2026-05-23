package com.example.billing.command;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceCommandHandler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public Invoice createInvoice(Invoice invoice) {
        InvoiceEntity entity = InvoiceEntity.create(
            invoice.getCustomerId(),
            invoice.getAmount(),
            invoice.getCustomerId() != null ? "Product-" + invoice.getCustomerId() : null
        );
        if (invoice.getStatus() != null) {
            entity.setStatus(invoice.getStatus().getValue());
        }
        InvoiceEntity saved = invoiceRepository.save(entity);
        return invoiceMapper.toModel(saved);
    }
}
