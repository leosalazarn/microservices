package com.example.products.infrastructure.interceptor;

import com.example.products.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingMessageDispatcherInterceptor implements MessageDispatcherInterceptor {

    @Override
    public void preDispatch(DomainEvent event) {
        log.info("Dispatching event: {} for aggregate: {}", 
            event.getClass().getSimpleName(), event.getAggregateId());
    }

    @Override
    public void postDispatch(DomainEvent event) {
        log.info("Event {} dispatched successfully", event.getClass().getSimpleName());
    }

    @Override
    public void onError(DomainEvent event, Exception error) {
        log.error("Event {} dispatch failed: {}", event.getClass().getSimpleName(), error.getMessage());
    }
}
