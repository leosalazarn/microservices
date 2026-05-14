package com.example.billing.query;

import com.example.billing.domain.entity.InvoiceEntity;
import com.example.billing.domain.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceQueryHandler {

    private final InvoiceRepository repository;

    public List<Invoice> getAllInvoices() {
        return repository.findAll()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    private Invoice toModel(InvoiceEntity entity) {
        Invoice invoice = new Invoice();
        invoice.setId(entity.getId());
        invoice.setCustomerId(entity.getCustomerId());
        invoice.setAmount(entity.getAmount());
        invoice.setStatus(Invoice.StatusEnum.fromValue(entity.getStatus()));
        return invoice;
    }
}
