package com.example.billing.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {

    @Mock
    private EventDispatcher eventDispatcher;

    @InjectMocks
    private ProductEventConsumer consumer;

    private String validEventJson;
    private String eventType;

    @BeforeEach
    void setUp() {
        validEventJson = "{\"id\":\"test-id\",\"name\":\"Test Product\",\"price\":100.0}";
        eventType = "ProductCreatedEvent";
    }

    @Test
    void handleEvent_ValidEvent_ShouldDispatchSuccessfully() {
        doNothing().when(eventDispatcher).dispatch(eventType, validEventJson);

        assertDoesNotThrow(() -> consumer.handleEvent(eventType, validEventJson));

        verify(eventDispatcher).dispatch(eventType, validEventJson);
    }

    @Test
    void handleEvent_InvalidJson_ShouldHandleGracefully() {
        String invalidJson = "invalid-json";
        doNothing().when(eventDispatcher).dispatch(eventType, invalidJson);

        assertDoesNotThrow(() -> consumer.handleEvent(eventType, invalidJson));

        verify(eventDispatcher).dispatch(eventType, invalidJson);
    }

    @Test
    void handleEvent_NullMessage_ShouldHandleGracefully() {
        doNothing().when(eventDispatcher).dispatch(eventType, null);

        assertDoesNotThrow(() -> consumer.handleEvent(eventType, null));

        verify(eventDispatcher).dispatch(eventType, null);
    }

    @Test
    void handleEvent_EmptyMessage_ShouldHandleGracefully() {
        String emptyJson = "";
        doNothing().when(eventDispatcher).dispatch(eventType, emptyJson);

        assertDoesNotThrow(() -> consumer.handleEvent(eventType, emptyJson));

        verify(eventDispatcher).dispatch(eventType, emptyJson);
    }

    @Test
    void handleEvent_DispatcherThrowsException_ShouldHandleGracefully() {
        doThrow(new RuntimeException("Dispatcher error")).when(eventDispatcher).dispatch(eventType, validEventJson);

        assertDoesNotThrow(() -> consumer.handleEvent(eventType, validEventJson));

        verify(eventDispatcher).dispatch(eventType, validEventJson);
    }
}
