package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.dto.EmergencyRecoveryRequest;
import com.healthcare.keymanagement.entity.KeyRecoveryRequest;
import com.healthcare.keymanagement.service.KeyRecoveryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/keys")
@RequiredArgsConstructor
@Tag(
        name = "Key Recovery",
        description = "Emergency key recovery operations restricted to administrators"
)
public class KeyRecoveryController {


    private final KeyRecoveryService keyRecoveryService;


    // ============================================================
    // 1. CREATE EMERGENCY RECOVERY REQUEST
    // ============================================================

    @Operation(
            summary = "Create emergency recovery request",
            description = """
                    Creates an emergency recovery request for a key.

                    The request records the reason for recovery and the
                    authenticated administrator who initiated the request.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Emergency recovery request created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid recovery request"
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
                    responseCode = "409",
                    description = "Recovery request cannot be created because of the current key state"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Key was not found"
            )
    })
    @PostMapping("/{keyId}/recover")
    public ResponseEntity<?> requestRecovery(

            @Parameter(
                    description = "Unique identifier of the key",
                    required = true,
                    example = "PHASE13-RECOVERY-001"
            )
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

    @Operation(
            summary = "Approve emergency recovery request",
            description = """
                    Approves a pending emergency recovery request.

                    Approval is restricted to administrators and is required
                    before the recovery operation can be executed.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Emergency recovery request approved"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid recovery request identifier"
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
                    responseCode = "409",
                    description = "Recovery request is not in a state that allows approval"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recovery request was not found"
            )
    })
    @PostMapping("/recovery/{requestId}/approve")
    public ResponseEntity<?> approveRecovery(

            @Parameter(
                    description = "Identifier of the emergency recovery request",
                    required = true,
                    example = "1"
            )
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

    @Operation(
            summary = "Execute emergency recovery",
            description = """
                    Executes an approved emergency recovery request.

                    The recovery request must be approved before execution.
                    The operation is restricted to administrators.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Emergency recovery executed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid recovery request identifier"
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
                    responseCode = "409",
                    description = "Recovery request must be approved before execution"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recovery request was not found"
            )
    })
    @PostMapping("/recovery/{requestId}/execute")
    public ResponseEntity<?> executeRecovery(

            @Parameter(
                    description = "Identifier of the approved emergency recovery request",
                    required = true,
                    example = "1"
            )
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