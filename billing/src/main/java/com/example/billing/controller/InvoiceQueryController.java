package com.example.billing.controller;

import com.example.billing.api.BillingQueryApi;
import com.example.billing.model.Invoice;
import com.example.billing.query.InvoiceQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvoiceQueryController implements BillingQueryApi {
    
    private final InvoiceQueryHandler queryHandler;
    
    @Override
    public ResponseEntity<String> getServiceInfo() {
        return ResponseEntity.ok("Hello from Billing Service");
    }
    
    @Override
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("OK");
    }
    
    @Override
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(queryHandler.getAllInvoices());
    }
}
