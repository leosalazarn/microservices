package com.example.billing.query;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceQueryHandlerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceQueryHandler queryHandler;

    @Test
    void getAllInvoices_EmptyList_ShouldReturnEmptyList() {
        when(invoiceRepository.findAll()).thenReturn(List.of());

        List<Invoice> result = queryHandler.getAllInvoices();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllInvoices_WithInvoices_ShouldReturnAllInvoices() {
        InvoiceEntity entity1 = InvoiceEntity.create(123L, 100.0, "P1");
        InvoiceEntity entity2 = InvoiceEntity.create(456L, 200.0, "P2");
        entity1.setId("1");
        entity2.setId("2");

        Invoice invoice1 = new Invoice();
        invoice1.setId("1");
        invoice1.setCustomerId(123L);
        invoice1.setAmount(100.0);
        Invoice invoice2 = new Invoice();
        invoice2.setId("2");
        invoice2.setCustomerId(456L);
        invoice2.setAmount(200.0);

        when(invoiceRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(invoiceMapper.toModel(entity1)).thenReturn(invoice1);
        when(invoiceMapper.toModel(entity2)).thenReturn(invoice2);

        List<Invoice> result = queryHandler.getAllInvoices();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(123L, result.get(0).getCustomerId());
        assertEquals(456L, result.get(1).getCustomerId());
    }
}
