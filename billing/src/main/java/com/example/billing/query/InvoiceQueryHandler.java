package com.example.billing.query;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceQueryHandler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll()
            .stream()
            .map(invoiceMapper::toModel)
            .toList();
    }
}
