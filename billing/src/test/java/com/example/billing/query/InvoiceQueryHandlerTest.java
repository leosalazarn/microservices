package com.example.billing.query;

import com.example.billing.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceQueryHandlerTest {

    @InjectMocks
    private InvoiceQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        // Reset the static list for each test
        queryHandler.getAllInvoices().clear();
    }

    @Test
    void getAllInvoices_EmptyList_ShouldReturnEmptyList() {
        List<Invoice> result = queryHandler.getAllInvoices();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllInvoices_WithInvoices_ShouldReturnAllInvoices() {
        // Add some test invoices
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setCustomerId(123L);
        invoice1.setAmount(100.0);

        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setCustomerId(456L);
        invoice2.setAmount(200.0);

        List<Invoice> invoices = queryHandler.getAllInvoices();
        invoices.add(invoice1);
        invoices.add(invoice2);

        List<Invoice> result = queryHandler.getAllInvoices();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(123L, result.get(0).getCustomerId());
        assertEquals(456L, result.get(1).getCustomerId());
    }

    @Test
    void getAllInvoices_MultipleInvocations_ShouldReturnSameList() {
        List<Invoice> result1 = queryHandler.getAllInvoices();
        List<Invoice> result2 = queryHandler.getAllInvoices();

        assertSame(result1, result2);
    }
}
