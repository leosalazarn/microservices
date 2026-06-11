package com.example.products.streams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryStats {
    String category;
    long count;
    double totalPrice;
    double minPrice;
    double maxPrice;

    @JsonIgnore
    public double getAveragePrice() {
        return count == 0 ? 0.0 : totalPrice / count;
    }

    public static CategoryStats empty() {
        return new CategoryStats(null, 0, 0.0, 0.0, 0.0);
    }

    public static CategoryStats initial(String category, double price) {
        return new CategoryStats(category, 1, price, price, price);
    }

    public CategoryStats merge(CategoryStats other) {
        return new CategoryStats(
                category,
                this.count + other.count,
                this.totalPrice + other.totalPrice,
                Math.min(this.minPrice, other.minPrice),
                Math.max(this.maxPrice, other.maxPrice)
        );
    }
}
