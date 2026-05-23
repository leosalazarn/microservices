package com.example.billing.command;

import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceMapper;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import com.example.billing.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceCommandHandlerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceCommandHandler commandHandler;

    private Invoice inputInvoice;
    private InvoiceEntity savedEntity;
    private Invoice resultInvoice;

    @BeforeEach
    void setUp() {
        inputInvoice = new Invoice();
        inputInvoice.setCustomerId(123L);
        inputInvoice.setAmount(100.0);

        savedEntity = InvoiceEntity.create(123L, 100.0, "Test");
        savedEntity.setId("mongo-id");

        resultInvoice = new Invoice();
        resultInvoice.setId("mongo-id");
        resultInvoice.setCustomerId(123L);
        resultInvoice.setAmount(100.0);
        resultInvoice.setStatus(Invoice.StatusEnum.PENDING);
    }

    @Test
    void createInvoice_ValidInvoice_ShouldReturnInvoiceWithId() {
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenReturn(savedEntity);
        when(invoiceMapper.toModel(savedEntity)).thenReturn(resultInvoice);

        Invoice result = commandHandler.createInvoice(inputInvoice);

        assertNotNull(result);
        assertEquals("mongo-id", result.getId());
        assertEquals(inputInvoice.getCustomerId(), result.getCustomerId());
        assertEquals(inputInvoice.getAmount(), result.getAmount());
        assertEquals(Invoice.StatusEnum.PENDING, result.getStatus());
    }

    @Test
    void createInvoice_NullFields_ShouldDefaultToPending() {
        Invoice nullInvoice = new Invoice();

        InvoiceEntity defaultEntity = InvoiceEntity.create(null, null, null);
        defaultEntity.setId("mongo-id");

        Invoice defaultResult = new Invoice();
        defaultResult.setId("mongo-id");
        defaultResult.setStatus(Invoice.StatusEnum.PENDING);

        when(invoiceRepository.save(any(InvoiceEntity.class))).thenReturn(defaultEntity);
        when(invoiceMapper.toModel(defaultEntity)).thenReturn(defaultResult);

        Invoice result = commandHandler.createInvoice(nullInvoice);

        assertNotNull(result);
        assertEquals(Invoice.StatusEnum.PENDING, result.getStatus());
    }

    @Test
    void createInvoice_MultipleInvocations_ShouldGenerateUniqueIds() {
        InvoiceEntity entity1 = InvoiceEntity.create(123L, 100.0, "Test");
        entity1.setId("id-1");
        InvoiceEntity entity2 = InvoiceEntity.create(123L, 100.0, "Test");
        entity2.setId("id-2");

        Invoice result1 = new Invoice();
        result1.setId("id-1");
        Invoice result2 = new Invoice();
        result2.setId("id-2");

        when(invoiceRepository.save(any(InvoiceEntity.class)))
            .thenReturn(entity1)
            .thenReturn(entity2);
        when(invoiceMapper.toModel(entity1)).thenReturn(result1);
        when(invoiceMapper.toModel(entity2)).thenReturn(result2);

        Invoice r1 = commandHandler.createInvoice(inputInvoice);
        Invoice r2 = commandHandler.createInvoice(inputInvoice);

        assertNotNull(r1.getId());
        assertNotNull(r2.getId());
        assertNotEquals(r1.getId(), r2.getId());
    }
}
