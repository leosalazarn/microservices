package com.example.products.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandBusTest {

    @Mock
    private Validator validator;

    @Mock
    private CommandHandler<TestCommand, String> commandHandler;

    @Mock
    private CommandInterceptor interceptor;

    private CommandBus commandBus;

    @BeforeEach
    void setUp() {
        List<CommandInterceptor> interceptors = List.of(interceptor);
        commandBus = new CommandBus(validator, interceptors);
    }

    @Test
    void dispatch_ValidCommand_ShouldReturnResult() {
        TestCommand command = new TestCommand("test");
        String expectedResult = "handled";
        Set<ConstraintViolation<TestCommand>> emptyViolations = Collections.emptySet();

        when(validator.validate(command)).thenReturn(emptyViolations);
        commandBus.registerHandler(TestCommand.class, commandHandler);
        when(commandHandler.handle(command)).thenReturn(expectedResult);

        String result = commandBus.dispatch(command);

        assertEquals(expectedResult, result);
        verify(validator).validate(command);
        verify(commandHandler).handle(command);
        verify(interceptor).preProcess(command);
        verify(interceptor).postProcess(command, expectedResult);
    }

    @Test
    void dispatch_HandlerNotFound_ShouldThrowException() {
        TestCommand command = new TestCommand("test");
        Set<ConstraintViolation<TestCommand>> emptyViolations = Collections.emptySet();

        when(validator.validate(command)).thenReturn(emptyViolations);

        assertThrows(IllegalArgumentException.class, () -> commandBus.dispatch(command));

        verify(validator).validate(command);
        verify(interceptor).preProcess(command);
        verify(interceptor).onError(eq(command), any(IllegalArgumentException.class));
    }

    @Test
    void dispatch_ValidationFails_ShouldThrowException() {
        TestCommand command = new TestCommand("test");
        @SuppressWarnings("unchecked")
        ConstraintViolation<TestCommand> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error");
        Set<ConstraintViolation<TestCommand>> violations = Set.of(violation);

        when(validator.validate(command)).thenReturn(violations);

        assertThrows(IllegalArgumentException.class, () -> commandBus.dispatch(command));

        verify(validator).validate(command);
        verify(commandHandler, never()).handle(any());
        verify(interceptor).preProcess(command);
        verify(interceptor).onError(eq(command), any(IllegalArgumentException.class));
    }

    @Test
    void dispatch_HandlerThrowsException_ShouldPropagateException() {
        TestCommand command = new TestCommand("test");
        Set<ConstraintViolation<TestCommand>> emptyViolations = Collections.emptySet();

        when(validator.validate(command)).thenReturn(emptyViolations);
        commandBus.registerHandler(TestCommand.class, commandHandler);
        when(commandHandler.handle(command)).thenThrow(new IllegalArgumentException("Invalid command"));

        assertThrows(IllegalArgumentException.class, () -> commandBus.dispatch(command));

        verify(validator).validate(command);
        verify(commandHandler).handle(command);
        verify(interceptor).preProcess(command);
        verify(interceptor).onError(eq(command), any(IllegalArgumentException.class));
    }

    // Test command for testing purposes
    @Getter
    private static class TestCommand implements Command {
        private final String value;

        public TestCommand(String value) {
            this.value = value;
        }

    }
}
