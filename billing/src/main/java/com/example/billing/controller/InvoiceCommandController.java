package com.example.billing.controller;

import com.example.billing.api.BillingCommandApi;
import com.example.billing.command.InvoiceCommandHandler;
import com.example.billing.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvoiceCommandController implements BillingCommandApi {
    
    private final InvoiceCommandHandler commandHandler;
    
    @Override
    public ResponseEntity<Invoice> createInvoice(Invoice invoice) {
        return ResponseEntity.status(201).body(commandHandler.createInvoice(invoice));
    }
}
