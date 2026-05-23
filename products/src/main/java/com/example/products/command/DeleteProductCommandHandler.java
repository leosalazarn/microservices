package com.example.products.command;

import com.example.products.domain.entity.ProductEntity;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, Void> {

    private final ProductRepository repository;

    @Override
    @Transactional
    public Void handle(DeleteProductCommand command) {
        log.info("Deleting product: id={}", command.getId());

        ProductEntity entity = repository.findByIdAndActiveTrue(command.getId())
                .orElseThrow(() -> new ProductNotFoundException(command.getId()));

        entity.setActive(false);
        repository.save(entity);

        log.info("Product deleted: id={}", command.getId());
        return null;
    }
}
