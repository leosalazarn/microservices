package com.example.billing.infrastructure.messaging;

public interface EventHandler<T> {
    void handle(T event);
    String getEventType();
}
