package com.example.products.infrastructure.interceptor;

import com.example.products.command.Command;
import com.example.products.command.CommandInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingCommandInterceptor implements CommandInterceptor {
    
    @Override
    public <C extends Command> void preProcess(C command) {
        log.info("Executing command: {}", command.getClass().getSimpleName());
    }
    
    @Override
    public <C extends Command, R> void postProcess(C command, R result) {
        log.info("Command {} completed successfully", command.getClass().getSimpleName());
    }
    
    @Override
    public <C extends Command> void onError(C command, Exception error) {
        log.error("Command {} failed: {}", command.getClass().getSimpleName(), error.getMessage());
    }
}
