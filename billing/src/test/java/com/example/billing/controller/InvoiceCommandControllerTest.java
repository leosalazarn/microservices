package com.example.billing.controller;

import com.example.billing.command.InvoiceCommandHandler;
import com.example.billing.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceCommandControllerTest {

    @Mock
    private InvoiceCommandHandler commandHandler;

    @InjectMocks
    private InvoiceCommandController controller;

    private Invoice inputInvoice;
    private Invoice createdInvoice;

    @BeforeEach
    void setUp() {
        inputInvoice = new Invoice();
        inputInvoice.setCustomerId(123L);
        inputInvoice.setAmount(100.0);

        createdInvoice = new Invoice();
        createdInvoice.setId(1L);
        createdInvoice.setCustomerId(123L);
        createdInvoice.setAmount(100.0);
    }

    @Test
    void createInvoice_ValidInvoice_ShouldReturnCreatedInvoice() {
        when(commandHandler.createInvoice(any(Invoice.class))).thenReturn(createdInvoice);

        ResponseEntity<Invoice> response = controller.createInvoice(inputInvoice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdInvoice.getId(), response.getBody().getId());
        assertEquals(createdInvoice.getCustomerId(), response.getBody().getCustomerId());
        assertEquals(createdInvoice.getAmount(), response.getBody().getAmount());

        verify(commandHandler).createInvoice(inputInvoice);
    }

    @Test
    void createInvoice_CommandHandlerThrowsException_ShouldPropagateException() {
        when(commandHandler.createInvoice(any(Invoice.class)))
                .thenThrow(new IllegalArgumentException("Invalid invoice data"));

        assertThrows(IllegalArgumentException.class, () -> controller.createInvoice(inputInvoice));

        verify(commandHandler).createInvoice(inputInvoice);
    }

    @Test
    void createInvoice_NullInvoice_ShouldHandleGracefully() {
        when(commandHandler.createInvoice(null)).thenReturn(createdInvoice);

        ResponseEntity<Invoice> response = controller.createInvoice(null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(commandHandler).createInvoice(null);
    }
}
