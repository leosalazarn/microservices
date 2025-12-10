package com.example.products.infrastructure.eventstore;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "event_store")
public class EventStoreEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String aggregateId;
    
    private String eventType;
    
    private String eventData;
    
    private Long version;
    
    private LocalDateTime occurredAt;
    
    private LocalDateTime storedAt;
}
