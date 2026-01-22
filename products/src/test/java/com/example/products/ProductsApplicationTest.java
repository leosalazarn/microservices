package com.example.products;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.vault.enabled=false"
})
@Disabled("Use ProductsIntegrationTest with Testcontainers instead")
class ProductsApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main() {
        ProductsApplication.class.getDeclaredMethods();
    }
}
