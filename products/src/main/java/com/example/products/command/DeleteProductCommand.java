package com.example.products.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductCommand implements Command {

    @NotBlank(message = "Product ID is required")
    private String id;
}
