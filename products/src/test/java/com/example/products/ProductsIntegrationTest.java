package com.example.products;

import com.example.products.domain.repository.ProductLookupRepository;
import com.example.products.domain.repository.ProductRepository;
import com.example.products.model.Product;
import com.example.products.model.ProductUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLookupRepository lookupRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        lookupRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /products")
    class GetAllProducts {

        @Test
        @DisplayName("should return empty list when no products exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return all products")
        void shouldReturnAllProducts() throws Exception {
            // Create products
            createProduct("Product 1", 99.99);
            createProduct("Product 2", 199.99);

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("Product 1", "Product 2")));
        }
    }

    @Nested
    @DisplayName("POST /products")
    class CreateProduct {

        @Test
        @DisplayName("should create product and return with ID")
        void shouldCreateProduct() throws Exception {
            Product product = new Product();
            product.setName("New Product");
            product.setPrice(149.99);
            product.setDescription("Test description");
            product.setCategory("Electronics");

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(product)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("New Product")))
                    .andExpect(jsonPath("$.price", is(149.99)))
                    .andExpect(jsonPath("$.description", is("Test description")))
                    .andExpect(jsonPath("$.category", is("Electronics")));
        }

        @Test
        @DisplayName("should persist to lookup table for duplicate validation")
        void shouldPersistToLookupTable() throws Exception {
            Product product = new Product();
            product.setName("Lookup Test");
            product.setPrice(99.99);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(product)))
                    .andExpect(status().isCreated());

            // Verify lookup table
            assert lookupRepository.existsByName("Lookup Test");
        }

        @Test
        @DisplayName("should reject duplicate product name")
        void shouldRejectDuplicateName() throws Exception {
            // Create first product
            createProduct("Duplicate Name", 99.99);

            // Try to create with same name - expect DuplicateProductException
            Product duplicate = new Product();
            duplicate.setName("Duplicate Name");
            duplicate.setPrice(199.99);

            Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
            );
            
            assert thrown.getCause() instanceof com.example.products.exception.DuplicateProductException;
        }

        @Test
        @DisplayName("should reject invalid product - missing name")
        void shouldRejectMissingName() throws Exception {
            Product product = new Product();
            product.setPrice(99.99);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(product)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should reject invalid product - price too low")
        void shouldRejectPriceTooLow() throws Exception {
            Product product = new Product();
            product.setName("Invalid Price");
            product.setPrice(0.001);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(product)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("should update product fully")
        void shouldUpdateProductFully() throws Exception {
            String productId = createProduct("Original Name", 99.99);

            ProductUpdate update = new ProductUpdate();
            update.setName("Updated Name");
            update.setPrice(149.99);
            update.setDescription("Updated description");
            update.setCategory("Updated category");

            mockMvc.perform(put("/products/" + productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId)))
                    .andExpect(jsonPath("$.name", is("Updated Name")))
                    .andExpect(jsonPath("$.price", is(149.99)))
                    .andExpect(jsonPath("$.description", is("Updated description")))
                    .andExpect(jsonPath("$.category", is("Updated category")));
        }

        @Test
        @DisplayName("should update product partially - price only")
        void shouldUpdatePriceOnly() throws Exception {
            String productId = createProduct("Keep This Name", 99.99);

            ProductUpdate update = new ProductUpdate();
            update.setPrice(199.99);

            mockMvc.perform(put("/products/" + productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Keep This Name")))
                    .andExpect(jsonPath("$.price", is(199.99)));
        }

        @Test
        @DisplayName("should update product partially - name only")
        void shouldUpdateNameOnly() throws Exception {
            String productId = createProduct("Old Name", 99.99);

            ProductUpdate update = new ProductUpdate();
            update.setName("New Name");

            mockMvc.perform(put("/products/" + productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("New Name")))
                    .andExpect(jsonPath("$.price", is(99.99)));
        }

        @Test
        @DisplayName("should throw ProductNotFoundException for non-existent product")
        void shouldThrowNotFoundForNonExistent() throws Exception {
            ProductUpdate update = new ProductUpdate();
            update.setPrice(99.99);

            Exception thrown = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(put("/products/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
            );
            
            assert thrown.getCause() instanceof com.example.products.exception.ProductNotFoundException;
        }
    }

    @Nested
    @DisplayName("GET /health")
    class HealthCheck {

        @Test
        @DisplayName("should return health status")
        void shouldReturnHealthStatus() throws Exception {
            mockMvc.perform(get("/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /")
    class ServiceInfo {

        @Test
        @DisplayName("should return service info")
        void shouldReturnServiceInfo() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk());
        }
    }

    // Helper method
    private String createProduct(String name, Double price) throws Exception {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);

        MvcResult result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn();

        Product created = objectMapper.readValue(result.getResponse().getContentAsString(), Product.class);
        return created.getId();
    }
}
