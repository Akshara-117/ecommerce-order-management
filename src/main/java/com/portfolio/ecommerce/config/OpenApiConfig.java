package com.portfolio.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI ecommerceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce Order Management API")
                .version("1.0")
                .description("Spring Boot REST API for products, carts, checkout, orders and JWT authentication. "
                    + "Register or log in under /api/auth, then use Authorize to provide your JWT."))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .schemaRequirement("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste the accessToken returned by POST /api/auth/login."));
    }
}
