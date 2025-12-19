package com.example.products.command;

public interface CommandInterceptor {
    
    <C extends Command> void preProcess(C command);
    
    <C extends Command, R> void postProcess(C command, R result);
    
    <C extends Command> void onError(C command, Exception error);
}
