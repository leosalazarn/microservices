package com.example.products.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommandBus {
    
    private final Validator validator;
    private final Map<Class<? extends Command>, CommandHandler<? extends Command, ?>> handlers = new HashMap<>();
    
    public <C extends Command, R> void registerHandler(Class<C> commandClass, CommandHandler<C, R> handler) {
        handlers.put(commandClass, handler);
    }
    
    @SuppressWarnings("unchecked")
    public <C extends Command, R> R dispatch(C command) {
        // Validate command before dispatching
        Set<ConstraintViolation<C>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Command validation failed: " + errors);
        }
        
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for command: " + command.getClass().getName());
        }
        return handler.handle(command);
    }
}
