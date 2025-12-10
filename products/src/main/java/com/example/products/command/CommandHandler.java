package com.example.products.command;

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}
