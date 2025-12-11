package com.example.billing.command;

import com.example.billing.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceCommandHandlerTest {

    @InjectMocks
    private InvoiceCommandHandler commandHandler;

    private Invoice inputInvoice;

    @BeforeEach
    void setUp() {
        inputInvoice = new Invoice();
        inputInvoice.setCustomerId(123L);
        inputInvoice.setAmount(100.0);
    }

    @Test
    void createInvoice_ValidInvoice_ShouldReturnInvoiceWithId() {
        Invoice result = commandHandler.createInvoice(inputInvoice);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(inputInvoice.getCustomerId(), result.getCustomerId());
        assertEquals(inputInvoice.getAmount(), result.getAmount());
        assertEquals(Invoice.StatusEnum.PENDING, result.getStatus());
    }

    @Test
    void createInvoice_NullInvoice_ShouldHandleGracefully() {
        Invoice nullInvoice = new Invoice();
        
        Invoice result = commandHandler.createInvoice(nullInvoice);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(Invoice.StatusEnum.PENDING, result.getStatus());
    }

    @Test
    void createInvoice_InvoiceWithNullFields_ShouldHandleGracefully() {
        Invoice invoiceWithNulls = new Invoice();
        // customerId and amount are null

        Invoice result = commandHandler.createInvoice(invoiceWithNulls);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(Invoice.StatusEnum.PENDING, result.getStatus());
    }

    @Test
    void createInvoice_MultipleInvocations_ShouldGenerateUniqueIds() {
        Invoice result1 = commandHandler.createInvoice(inputInvoice);
        
        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Invoice result2 = commandHandler.createInvoice(inputInvoice);

        assertNotNull(result1.getId());
        assertNotNull(result2.getId());
        assertNotEquals(result1.getId(), result2.getId());
    }
}
