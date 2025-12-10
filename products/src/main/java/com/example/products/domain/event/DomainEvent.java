package com.example.products.domain.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    String getAggregateId();
    LocalDateTime getOccurredAt();
    Long getVersion();
}
