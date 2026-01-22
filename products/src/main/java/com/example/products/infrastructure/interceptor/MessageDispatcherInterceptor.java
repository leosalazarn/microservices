package com.example.products.infrastructure.interceptor;

import com.example.products.domain.event.DomainEvent;

public interface MessageDispatcherInterceptor {

    void preDispatch(DomainEvent event);

    void postDispatch(DomainEvent event);

    void onError(DomainEvent event, Exception error);
}
