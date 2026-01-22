package com.example.products.infrastructure.messaging;

import com.example.products.domain.event.ProductCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventPublisher eventPublisher;

    private ProductCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = ProductCreatedEvent.of("test-id", "Test Product", 100.0, 1L);
    }

    @Test
    void publishProductCreatedEvent_ValidEvent_ShouldPublishSuccessfully() throws JsonProcessingException {
        String expectedJson = "{\"id\":\"test-id\",\"name\":\"Test Product\",\"price\":100.0,\"version\":1}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);

        eventPublisher.publishProductCreatedEvent(event);

        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void publishProductCreatedEvent_JsonProcessingException_ShouldHandleGracefully() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        // Should not throw - exception is caught and logged
        eventPublisher.publishProductCreatedEvent(event);

        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void publishProductCreatedEvent_NullEvent_ShouldHandleGracefully() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(null)).thenReturn("null");

        eventPublisher.publishProductCreatedEvent(null);

        verify(objectMapper).writeValueAsString(null);
        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void publishProductCreatedEvent_KafkaException_ShouldHandleGracefully() throws JsonProcessingException {
        String expectedJson = "{\"id\":\"test-id\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("Kafka error"));

        // Should not throw - exception is caught and logged
        eventPublisher.publishProductCreatedEvent(event);

        verify(kafkaTemplate).send(any(Message.class));
    }
}
