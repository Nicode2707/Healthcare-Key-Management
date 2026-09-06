package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.dto.KeyMetadataRequest;
import com.healthcare.keymanagement.dto.KeyMetadataResponse;
import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.service.KeyMetadataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
@Tag(
        name = "Key Management",
        description = "APIs for AES-256 cryptographic key lifecycle management"
)
@SecurityRequirement(name = "bearerAuth")
public class KeyMetadataController {

    private final KeyMetadataService service;


    // =========================================================
    // CREATE KEY
    // =========================================================

    @Operation(
            summary = "Create a new AES-256 key",
            description = """
                    Generates a new AES-256 cryptographic key,
                    protects the key before storage, and creates
                    the initial key metadata with version 1.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AES-256 key created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = KeyMetadataResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            )
    })
    @PostMapping
    public KeyMetadataResponse create(
            @Valid @RequestBody KeyMetadataRequest request) {

        KeyMetadata keyMetadata = KeyMetadata.builder()
                .keyId(request.getKeyId())
                .algorithm(request.getAlgorithm())
                .expiresAt(request.getExpiresAt())
                .build();

        KeyMetadata saved =
                service.create(keyMetadata);

        return toResponse(saved);
    }


    // =========================================================
    // GET ALL KEYS
    // =========================================================

    @Operation(
            summary = "Get all key metadata",
            description = """
                    Retrieves metadata for all cryptographic keys.
                    Protected key material is never returned by this API.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key metadata retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = KeyMetadataResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            )
    })
    @GetMapping
    public List<KeyMetadataResponse> getAll() {

        return service.getAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // REVOKE KEY
    // =========================================================

    @Operation(
            summary = "Revoke an active key",
            description = """
                    Revokes the currently active key version.
                    Once revoked, the key cannot be used as the
                    active key for normal operations.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key revoked successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = KeyMetadataResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Active key not found",
                    content = @Content
            )
    })
    @PatchMapping("/{keyId}/revoke")
    public KeyMetadataResponse revokeKey(

            @Parameter(
                    description = "Logical identifier of the key",
                    example = "PATIENT-DATA-001"
            )
            @PathVariable String keyId) {

        KeyMetadata revoked =
                service.revokeKey(keyId);

        return toResponse(revoked);
    }


    // =========================================================
    // ROTATE KEY
    // =========================================================

    @Operation(
            summary = "Rotate an active key",
            description = """
                    Rotates the active AES-256 key.
                    The current active version is marked as ROTATED
                    and a new active version is generated.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key rotated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = KeyMetadataResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Active key not found",
                    content = @Content
            )
    })
    @PatchMapping("/{keyId}/rotate")
    public KeyMetadataResponse rotateKey(

            @Parameter(
                    description = "Logical identifier of the key",
                    example = "PATIENT-DATA-001"
            )
            @PathVariable String keyId) {

        KeyMetadata rotated =
                service.rotateKey(keyId);

        return toResponse(rotated);
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private KeyMetadataResponse toResponse(
            KeyMetadata key) {

        return new KeyMetadataResponse(
                key.getId(),
                key.getKeyId(),
                key.getAlgorithm(),
                key.getKeyVersion(),
                key.getStatus(),
                key.getCreatedAt(),
                key.getExpiresAt(),
                key.getRevokedAt()
        );
    }
}