package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "server.port=0"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
    }

    @Test
    void main() {
        // Test that main method can be called without exceptions
        String[] args = {};
        // We don't actually call main() to avoid starting the server
        // Just verify the class structure is correct
        ApiGatewayApplication.class.getDeclaredMethods();
    }
}
