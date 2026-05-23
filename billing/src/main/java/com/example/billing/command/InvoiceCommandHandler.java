package com.example.billing.command;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceCommandHandler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public Invoice createInvoice(Invoice invoice) {
        log.info("Creating invoice for customer: {}, amount: {}", invoice.getCustomerId(), invoice.getAmount());
        InvoiceEntity entity = InvoiceEntity.create(
            invoice.getCustomerId(),
            invoice.getAmount(),
            invoice.getCustomerId() != null ? "Product-" + invoice.getCustomerId() : null
        );
        if (invoice.getStatus() != null) {
            entity.setStatus(invoice.getStatus().getValue());
        }
        InvoiceEntity saved = invoiceRepository.save(entity);
        log.info("Invoice created: id={}", saved.getId());
        return invoiceMapper.toModel(saved);
    }
}
