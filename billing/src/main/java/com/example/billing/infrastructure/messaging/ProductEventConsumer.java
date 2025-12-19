package com.example.billing.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {
    
    private final EventDispatcher eventDispatcher;
    
    @KafkaListener(topics = "product-events", groupId = "billing-service")
    public void handleEvent(@Header("eventType") String eventType, String payload) {
        log.info("Received event: {}", eventType);
        eventDispatcher.dispatch(eventType, payload);
    }
}
