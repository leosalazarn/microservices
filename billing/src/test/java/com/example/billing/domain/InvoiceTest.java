package com.example.billing.domain;

import com.example.billing.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
    }

    @Test
    void constructor_ShouldCreateInvoice() {
        assertNotNull(invoice);
        assertNull(invoice.getId());
        assertNull(invoice.getCustomerId());
        assertNull(invoice.getAmount());
        assertNull(invoice.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Long id = 123L;
        Long customerId = 456L;
        Double amount = 100.0;
        InvoiceStatus status = InvoiceStatus.PENDING;

        invoice.setId(id);
        invoice.setCustomerId(customerId);
        invoice.setAmount(amount);
        invoice.setStatus(status);

        assertEquals(id, invoice.getId());
        assertEquals(customerId, invoice.getCustomerId());
        assertEquals(amount, invoice.getAmount());
        assertEquals(status, invoice.getStatus());
    }

    @Test
    void equals_SameId_ShouldReturnTrue() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        
        Invoice invoice2 = new Invoice();
        invoice2.setId(1L);

        assertEquals(invoice1, invoice2);
    }

    @Test
    void equals_DifferentId_ShouldReturnFalse() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        
        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);

        assertNotEquals(invoice1, invoice2);
    }

    @Test
    void hashCode_SameId_ShouldReturnSameHashCode() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        
        Invoice invoice2 = new Invoice();
        invoice2.setId(1L);

        assertEquals(invoice1.hashCode(), invoice2.hashCode());
    }
}
