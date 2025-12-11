package com.example.billing.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceStatusTest {

    @Test
    void values_ShouldReturnAllStatuses() {
        InvoiceStatus[] statuses = InvoiceStatus.values();

        assertEquals(3, statuses.length);
        assertEquals(InvoiceStatus.PENDING, statuses[0]);
        assertEquals(InvoiceStatus.PAID, statuses[1]);
        assertEquals(InvoiceStatus.CANCELLED, statuses[2]);
    }

    @Test
    void valueOf_ValidStatus_ShouldReturnCorrectEnum() {
        assertEquals(InvoiceStatus.PENDING, InvoiceStatus.valueOf("PENDING"));
        assertEquals(InvoiceStatus.PAID, InvoiceStatus.valueOf("PAID"));
        assertEquals(InvoiceStatus.CANCELLED, InvoiceStatus.valueOf("CANCELLED"));
    }

    @Test
    void valueOf_InvalidStatus_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> InvoiceStatus.valueOf("INVALID"));
    }

    @Test
    void toString_ShouldReturnStatusName() {
        assertEquals("PENDING", InvoiceStatus.PENDING.toString());
        assertEquals("PAID", InvoiceStatus.PAID.toString());
        assertEquals("CANCELLED", InvoiceStatus.CANCELLED.toString());
    }
}
