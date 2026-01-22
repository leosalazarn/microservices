package com.example.products.infrastructure.eventstore;

import com.example.products.domain.event.DomainEvent;
import com.example.products.domain.event.ProductCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoEventStoreTest {

    @Mock
    private EventStoreRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MongoEventStore eventStore;

    private ProductCreatedEvent event;
    private EventStoreEntity entity;

    @BeforeEach
    void setUp() {
        event = ProductCreatedEvent.of("test-id", "Test Product", 100.0, 1L);
        
        entity = new EventStoreEntity();
        entity.setId("entity-id");
        entity.setAggregateId("test-id");
        entity.setEventType("ProductCreatedEvent");
        entity.setEventData("{\"productId\":\"test-id\"}");
        entity.setOccurredAt(LocalDateTime.now());
        entity.setVersion(1L);
    }

    @Test
    void save_ValidEvent_ShouldSaveSuccessfully() throws JsonProcessingException {
        String eventJson = "{\"productId\":\"test-id\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        when(repository.save(any(EventStoreEntity.class))).thenReturn(entity);

        eventStore.save(event);

        verify(objectMapper).writeValueAsString(event);
        verify(repository).save(any(EventStoreEntity.class));
    }

    @Test
    void save_JsonProcessingException_ShouldThrowRuntimeException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        assertThrows(RuntimeException.class, () -> eventStore.save(event));

        verify(objectMapper).writeValueAsString(event);
        verify(repository, never()).save(any(EventStoreEntity.class));
    }

    @Test
    void saveAll_ValidEvents_ShouldSaveAllSuccessfully() throws JsonProcessingException {
        ProductCreatedEvent event2 = ProductCreatedEvent.of("test-id-2", "Test Product 2", 200.0, 1L);
        List<DomainEvent> events = Arrays.asList(event, event2);
        String eventJson = "{\"productId\":\"test-id\"}";
        
        when(objectMapper.writeValueAsString(any())).thenReturn(eventJson);
        when(repository.save(any(EventStoreEntity.class))).thenReturn(entity);

        eventStore.saveAll(events);

        verify(objectMapper, times(2)).writeValueAsString(any());
        verify(repository, times(2)).save(any(EventStoreEntity.class));
    }

    @Test
    void saveAll_EmptyList_ShouldNotCallRepository() {
        List<DomainEvent> emptyEvents = Arrays.asList();

        eventStore.saveAll(emptyEvents);

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(repository);
    }

    @Test
    void saveAll_NullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> eventStore.saveAll(null));
    }

    @Test
    void save_NullEvent_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> eventStore.save(null));
    }

    @Test
    void save_RepositoryException_ShouldPropagateException() throws JsonProcessingException {
        String eventJson = "{\"productId\":\"test-id\"}";
        
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
        when(repository.save(any(EventStoreEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> eventStore.save(event));

        verify(objectMapper).writeValueAsString(event);
        verify(repository).save(any(EventStoreEntity.class));
    }
}
