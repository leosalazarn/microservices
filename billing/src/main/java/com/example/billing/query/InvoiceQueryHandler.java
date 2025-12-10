package com.example.billing.query;

import com.example.billing.model.Invoice;
import com.example.billing.model.Invoice.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceQueryHandler {
    
    public List<Invoice> getAllInvoices() {
        // Mock data - replace with actual repository
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setCustomerId(100L);
        invoice1.setAmount(299.99);
        invoice1.setStatus(StatusEnum.PAID);
        
        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setCustomerId(101L);
        invoice2.setAmount(149.99);
        invoice2.setStatus(StatusEnum.PENDING);
        
        return List.of(invoice1, invoice2);
    }
}
