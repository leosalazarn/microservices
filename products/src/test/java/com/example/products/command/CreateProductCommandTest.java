package com.example.products.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateProductCommandTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyCommand() {
        CreateProductCommand command = new CreateProductCommand();

        assertNotNull(command);
        assertNull(command.getName());
        assertNull(command.getPrice());
        assertNull(command.getDescription());
        assertNull(command.getCategory());
    }

    @Test
    void constructor_AllArgs_ShouldCreateCommandWithAllFields() {
        String name = "Test Product";
        Double price = 100.0;
        String description = "Test Description";
        String category = "Test Category";

        CreateProductCommand command = new CreateProductCommand(name, price, description, category);

        assertEquals(name, command.getName());
        assertEquals(price, command.getPrice());
        assertEquals(description, command.getDescription());
        assertEquals(category, command.getCategory());
    }

    @Test
    void validation_ValidCommand_ShouldPassValidation() {
        CreateProductCommand command = new CreateProductCommand(
            "Valid Product Name",
            99.99,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_NullName_ShouldFailValidation() {
        CreateProductCommand command = new CreateProductCommand(
            null,
            99.99,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Product name is required")));
    }

    @Test
    void validation_EmptyName_ShouldFailValidation() {
        CreateProductCommand command = new CreateProductCommand(
            "",
            99.99,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 1 and 100 characters")));
    }

    @Test
    void validation_TooLongName_ShouldFailValidation() {
        String longName = "A".repeat(101);
        CreateProductCommand command = new CreateProductCommand(
            longName,
            99.99,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 1 and 100 characters")));
    }

    @Test
    void validation_NullPrice_ShouldFailValidation() {
        CreateProductCommand command = new CreateProductCommand(
            "Valid Product Name",
            null,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Product price is required")));
    }

    @Test
    void validation_TooLowPrice_ShouldFailValidation() {
        CreateProductCommand command = new CreateProductCommand(
            "Valid Product Name",
            0.0,
            "Valid description",
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must be at least 0.01")));
    }

    @Test
    void validation_TooLongDescription_ShouldFailValidation() {
        String longDescription = "A".repeat(501);
        CreateProductCommand command = new CreateProductCommand(
            "Valid Product Name",
            99.99,
            longDescription,
            "Electronics"
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("cannot exceed 500 characters")));
    }

    @Test
    void validation_TooLongCategory_ShouldFailValidation() {
        String longCategory = "A".repeat(51);
        CreateProductCommand command = new CreateProductCommand(
            "Valid Product Name",
            99.99,
            "Valid description",
            longCategory
        );

        Set<ConstraintViolation<CreateProductCommand>> violations = validator.validate(command);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("cannot exceed 50 characters")));
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        CreateProductCommand command = new CreateProductCommand();
        String name = "Test Product";
        Double price = 100.0;
        String description = "Test Description";
        String category = "Test Category";

        command.setName(name);
        command.setPrice(price);
        command.setDescription(description);
        command.setCategory(category);

        assertEquals(name, command.getName());
        assertEquals(price, command.getPrice());
        assertEquals(description, command.getDescription());
        assertEquals(category, command.getCategory());
    }
}
