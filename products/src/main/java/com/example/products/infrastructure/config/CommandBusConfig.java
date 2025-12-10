package com.example.products.infrastructure.config;

import com.example.products.command.CommandBus;
import com.example.products.command.CreateProductCommand;
import com.example.products.command.CreateProductCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class CommandBusConfig {
    
    private final CommandBus commandBus;
    private final CreateProductCommandHandler createProductCommandHandler;
    
    @PostConstruct
    public void registerHandlers() {
        commandBus.registerHandler(CreateProductCommand.class, createProductCommandHandler);
    }
}
