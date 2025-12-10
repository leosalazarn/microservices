package com.example.products.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Products API")
                        .description("API for product management operations")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080/products").description("Via API Gateway"),
                        new Server().url("http://localhost:8081").description("Direct Access")
                ));
    }
}
