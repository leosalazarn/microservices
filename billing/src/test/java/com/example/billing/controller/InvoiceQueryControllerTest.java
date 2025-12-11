package com.example.billing.controller;

import com.example.billing.model.Invoice;
import com.example.billing.query.InvoiceQueryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceQueryControllerTest {

    @Mock
    private InvoiceQueryHandler queryHandler;

    @InjectMocks
    private InvoiceQueryController controller;

    private List<Invoice> mockInvoices;

    @BeforeEach
    void setUp() {
        Invoice invoice1 = new Invoice();
        invoice1.setId(1L);
        invoice1.setCustomerId(123L);
        invoice1.setAmount(100.0);

        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setCustomerId(456L);
        invoice2.setAmount(200.0);

        mockInvoices = Arrays.asList(invoice1, invoice2);
    }

    @Test
    void getAllInvoices_ShouldReturnInvoiceList() {
        when(queryHandler.getAllInvoices()).thenReturn(mockInvoices);

        ResponseEntity<List<Invoice>> response = controller.getAllInvoices();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(123L, response.getBody().get(0).getCustomerId());
        assertEquals(456L, response.getBody().get(1).getCustomerId());

        verify(queryHandler).getAllInvoices();
    }

    @Test
    void getAllInvoices_EmptyList_ShouldReturnEmptyList() {
        when(queryHandler.getAllInvoices()).thenReturn(Arrays.asList());

        ResponseEntity<List<Invoice>> response = controller.getAllInvoices();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(queryHandler).getAllInvoices();
    }

    @Test
    void getAllInvoices_QueryHandlerThrowsException_ShouldPropagateException() {
        when(queryHandler.getAllInvoices()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> controller.getAllInvoices());

        verify(queryHandler).getAllInvoices();
    }
}
