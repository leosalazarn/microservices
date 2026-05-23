package com.example.billing.query;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceQueryHandler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public List<Invoice> getAllInvoices() {
        log.debug("Fetching all invoices");
        List<Invoice> invoices = invoiceRepository.findAll()
            .stream()
            .map(invoiceMapper::toModel)
            .toList();
        log.debug("Found {} invoices", invoices.size());
        return invoices;
    }
}
