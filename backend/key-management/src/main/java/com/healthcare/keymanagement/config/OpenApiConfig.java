package com.healthcare.keymanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Healthcare IoT Key Management API",
                version = "1.0.0",
                description = """
                        REST API for secure AES-256 cryptographic key
                        lifecycle management in Healthcare Internet of Things.

                        The system provides secure key generation,
                        protection, storage, rotation, revocation,
                        expiration, archival and recovery operations.

                        Authentication is based on JWT Bearer tokens.
                        """,
                contact = @Contact(
                        name = "Healthcare Key Management Project"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Enter your JWT Bearer token"
)
public class OpenApiConfig {
}