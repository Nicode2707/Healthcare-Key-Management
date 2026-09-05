package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.dto.EmergencyRecoveryRequest;
import com.healthcare.keymanagement.entity.KeyRecoveryRequest;
import com.healthcare.keymanagement.service.KeyRecoveryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/keys")
@RequiredArgsConstructor
public class KeyRecoveryController {

    private final KeyRecoveryService keyRecoveryService;


    // ============================================================
    // 1. CREATE EMERGENCY RECOVERY REQUEST
    // ============================================================

    @PostMapping("/{keyId}/recover")
    public ResponseEntity<?> requestRecovery(
            @PathVariable String keyId,
            @RequestBody EmergencyRecoveryRequest request,
            Authentication authentication
    ) {

        try {

            KeyRecoveryRequest recoveryRequest =
                    keyRecoveryService.createRecoveryRequest(
                            keyId,
                            request,
                            authentication.getName()
                    );

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(
                            Map.of(
                                    "status", 202,
                                    "message",
                                    "Emergency recovery request created",
                                    "requestId",
                                    recoveryRequest.getId(),
                                    "keyId",
                                    recoveryRequest.getKeyId(),
                                    "requestedBy",
                                    recoveryRequest.getRequestedBy(),
                                    "reason",
                                    recoveryRequest.getReason(),
                                    "recoveryStatus",
                                    recoveryRequest.getStatus(),
                                    "requestedAt",
                                    recoveryRequest.getRequestedAt()
                            )
                    );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status", 400,
                                    "error", "Bad Request",
                                    "message",
                                    exception.getMessage()
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


    // ============================================================
    // 2. APPROVE EMERGENCY RECOVERY REQUEST
    // ============================================================

    @PostMapping("/recovery/{requestId}/approve")
    public ResponseEntity<?> approveRecovery(
            @PathVariable Long requestId,
            Authentication authentication
    ) {

        try {

            KeyRecoveryRequest recoveryRequest =
                    keyRecoveryService.approveRecoveryRequest(
                            requestId,
                            authentication.getName()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "status", 200,
                            "message",
                            "Emergency recovery request approved",
                            "requestId",
                            recoveryRequest.getId(),
                            "keyId",
                            recoveryRequest.getKeyId(),
                            "approvedBy",
                            authentication.getName(),
                            "recoveryStatus",
                            recoveryRequest.getStatus(),
                            "processedAt",
                            recoveryRequest.getProcessedAt()
                    )
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status", 400,
                                    "error", "Bad Request",
                                    "message",
                                    exception.getMessage()
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


    // ============================================================
    // 3. EXECUTE EMERGENCY RECOVERY
    // ============================================================

    @PostMapping("/recovery/{requestId}/execute")
    public ResponseEntity<?> executeRecovery(
            @PathVariable Long requestId,
            Authentication authentication
    ) {

        try {

            KeyRecoveryRequest recoveryRequest =
                    keyRecoveryService.executeRecovery(
                            requestId,
                            authentication.getName()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "status", 200,
                            "message",
                            "Emergency recovery executed successfully",
                            "requestId",
                            recoveryRequest.getId(),
                            "keyId",
                            recoveryRequest.getKeyId(),
                            "executedBy",
                            authentication.getName(),
                            "recoveryStatus",
                            recoveryRequest.getStatus(),
                            "processedAt",
                            recoveryRequest.getProcessedAt()
                    )
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status", 400,
                                    "error", "Bad Request",
                                    "message",
                                    exception.getMessage()
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