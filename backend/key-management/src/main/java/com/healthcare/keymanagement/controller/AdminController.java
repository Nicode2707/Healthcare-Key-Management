package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.service.KeyMetadataService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KeyMetadataService keyMetadataService;

    // ============================================================
    // ADMIN ACCESS CHECK
    // ============================================================

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

    @PostMapping("/keys/{keyId}/archive")
    public ResponseEntity<?> archiveKey(
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