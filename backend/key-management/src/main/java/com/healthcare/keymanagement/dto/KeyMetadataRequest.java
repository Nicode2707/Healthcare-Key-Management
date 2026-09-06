package com.healthcare.keymanagement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class KeyMetadataRequest {

    @NotBlank(message = "Key ID is required")
    @Size(min = 3, max = 100, message = "Key ID must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Key ID may contain only letters, numbers, hyphens, and underscores"
    )
    private String keyId;

    @NotBlank(message = "Algorithm is required")
    @Pattern(
            regexp = "^AES-256$",
            message = "Algorithm must be AES-256"
    )
    private String algorithm;

    @NotNull(message = "Expiration time is required")
    @Future(message = "Expiration time must be in the future")
    private LocalDateTime expiresAt;
}