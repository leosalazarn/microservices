package com.example.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EventDispatcher {
    
    private final ObjectMapper objectMapper;
    private final Map<String, EventHandler<?>> handlers;
    
    public EventDispatcher(ObjectMapper objectMapper, List<EventHandler<?>> handlerList) {
        this.objectMapper = objectMapper;
        this.handlers = handlerList.stream()
            .collect(Collectors.toMap(EventHandler::getEventType, Function.identity()));
    }
    
    @SuppressWarnings("unchecked")
    public void dispatch(String eventType, String payload) {
        try {
            EventHandler<Object> handler = (EventHandler<Object>) handlers.get(eventType);
            if (handler == null) {
                log.warn("No handler found for event type: {}", eventType);
                return;
            }
            
            Object event = deserializeEvent(payload, eventType);
            handler.handle(event);
            
        } catch (Exception e) {
            log.error("Failed to dispatch event {}: {}", eventType, e.getMessage());
        }
    }
    
    private Object deserializeEvent(String payload, String eventType) throws Exception {
        // Simple mapping - in real implementation, use event registry
        if ("ProductCreatedEvent".equals(eventType)) {
            return objectMapper.readValue(payload, com.example.billing.domain.event.ProductEvent.class);
        }
        throw new IllegalArgumentException("Unknown event type: " + eventType);
    }
}
