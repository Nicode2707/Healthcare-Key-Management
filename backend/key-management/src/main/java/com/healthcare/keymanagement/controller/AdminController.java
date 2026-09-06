package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.service.KeyMetadataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(
        name = "Admin",
        description = "Administrative operations for healthcare IoT key management"
)
public class AdminController {

    private final KeyMetadataService keyMetadataService;


    // ============================================================
    // ADMIN ACCESS CHECK
    // ============================================================

    @Operation(
            summary = "Check administrator access",
            description = "Verifies that the authenticated user has ADMIN privileges."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Administrator access granted"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have administrator privileges"
            )
    })
    @GetMapping("/check")
    public Map<String, Object> adminCheck(
            Authentication authentication
    ) {

        return Map.of(
                "message", "Admin access granted",
                "username", authentication.getName(),
                "role", authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );
    }


    // ============================================================
    // PHASE 12 — KEY ARCHIVAL
    // ============================================================

    @Operation(
            summary = "Archive a key",
            description = """
                    Archives the historical versions of a key.

                    An active key cannot be archived directly.
                    The operation is restricted to administrators.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Key archived successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Administrator privileges are required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Key was not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Key cannot be archived because of its current lifecycle state"
            )
    })
    @PostMapping("/keys/{keyId}/archive")
    public ResponseEntity<?> archiveKey(

            @Parameter(
                    description = "Unique identifier of the key",
                    required = true,
                    example = "PHASE12-ARCHIVE-001"
            )
            @PathVariable String keyId
    ) {

        try {

            int archivedKeys =
                    keyMetadataService.archiveKey(keyId);

            return ResponseEntity.ok(
                    Map.of(
                            "status", 200,
                            "message", "Key archived successfully",
                            "keyId", keyId,
                            "archivedKeys", archivedKeys
                    )
            );

        } catch (IllegalStateException exception) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            Map.of(
                                    "status", 409,
                                    "error", "Conflict",
                                    "message",
                                    exception.getMessage()
                            )
                    );
        }
    }
}