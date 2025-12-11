package com.example.billing.infrastructure.messaging;

import com.example.billing.domain.event.ProductEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductEventConsumer consumer;

    private String validEventJson;
    private ProductEvent productEvent;

    @BeforeEach
    void setUp() {
        validEventJson = "{\"id\":\"test-id\",\"name\":\"Test Product\",\"price\":100.0}";
        
        productEvent = new ProductEvent();
        productEvent.setId("test-id");
        productEvent.setName("Test Product");
        productEvent.setPrice(100.0);
    }

    @Test
    void handleProductCreated_ValidJson_ShouldProcessSuccessfully() throws JsonProcessingException {
        when(objectMapper.readValue(validEventJson, ProductEvent.class)).thenReturn(productEvent);

        assertDoesNotThrow(() -> consumer.handleProductCreated(validEventJson));

        verify(objectMapper).readValue(validEventJson, ProductEvent.class);
    }

    @Test
    void handleProductCreated_InvalidJson_ShouldHandleGracefully() throws JsonProcessingException {
        String invalidJson = "invalid-json";
        
        when(objectMapper.readValue(invalidJson, ProductEvent.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        assertDoesNotThrow(() -> consumer.handleProductCreated(invalidJson));

        verify(objectMapper).readValue(invalidJson, ProductEvent.class);
    }

    @Test
    void handleProductCreated_NullMessage_ShouldHandleGracefully() throws JsonProcessingException {
        when(objectMapper.readValue((String) null, ProductEvent.class)).thenReturn(null);

        assertDoesNotThrow(() -> consumer.handleProductCreated(null));

        verify(objectMapper).readValue((String) null, ProductEvent.class);
    }

    @Test
    void handleProductCreated_EmptyMessage_ShouldHandleGracefully() throws JsonProcessingException {
        String emptyJson = "";
        
        when(objectMapper.readValue(emptyJson, ProductEvent.class))
                .thenThrow(new JsonProcessingException("Empty JSON") {});

        assertDoesNotThrow(() -> consumer.handleProductCreated(emptyJson));

        verify(objectMapper).readValue(emptyJson, ProductEvent.class);
    }

    @Test
    void handleProductCreated_ObjectMapperThrowsRuntimeException_ShouldHandleGracefully() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertDoesNotThrow(() -> consumer.handleProductCreated(validEventJson));

        verify(objectMapper).readValue(validEventJson, ProductEvent.class);
    }
}
