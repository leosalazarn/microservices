package com.example.billing.infrastructure.messaging;

import com.example.billing.domain.event.ProductEvent;
import com.example.billing.infrastructure.repository.InvoiceEntity;
import com.example.billing.infrastructure.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedEventHandler implements EventHandler<ProductEvent> {

    private final InvoiceRepository invoiceRepository;

    @Override
    public void handle(ProductEvent event) {
        log.info("SAGA: Processing product created event for '{}' with price: {}", event.getName(), event.getPrice());

        InvoiceEntity invoice = InvoiceEntity.create(
            0L,
            event.getPrice(),
            event.getName()
        );

        invoiceRepository.save(invoice);
        log.info("SAGA: Invoice created for product '{}'", event.getName());
    }

    @Override
    public String getEventType() {
        return "ProductCreatedEvent";
    }
}
