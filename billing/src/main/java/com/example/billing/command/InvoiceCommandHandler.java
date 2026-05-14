package com.example.billing.command;

import com.example.billing.model.Invoice;
import com.example.billing.model.Invoice.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceCommandHandler {
    
    public Invoice createInvoice(Invoice invoice) {
        // Mock implementation - replace with actual repository
        invoice.setId(System.currentTimeMillis());
        if (invoice.getStatus() == null) {
            invoice.setStatus(StatusEnum.PENDING);
        }
        return invoice;
    }
}
