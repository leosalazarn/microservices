package com.example.products.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductCommand implements Command {
    private String name;
    private Double price;
    private String description;
    private String category;
}
