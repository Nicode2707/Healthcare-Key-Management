package com.healthcare.keymanagement;

import com.healthcare.keymanagement.controller.KeyRecoveryController;
import com.healthcare.keymanagement.dto.EmergencyRecoveryRequest;
import com.healthcare.keymanagement.entity.KeyRecoveryRequest;
import com.healthcare.keymanagement.service.KeyRecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class KeyRecoveryControllerTest {

    private KeyRecoveryService keyRecoveryService;
    private Authentication authentication;
    private EmergencyRecoveryRequest recoveryRequest;
    private KeyRecoveryController controller;

    @BeforeEach
    void setUp() {

        keyRecoveryService = mock(KeyRecoveryService.class);
        authentication = mock(Authentication.class);
        recoveryRequest = mock(EmergencyRecoveryRequest.class);

        controller = new KeyRecoveryController(keyRecoveryService);
    }

    // ---------------------------------------------------------
    // REQUEST RECOVERY
    // ---------------------------------------------------------

    @Test
    void requestRecoveryShouldReturnAccepted() {

        String keyId = "PHASE17-RECOVERY-001";

        KeyRecoveryRequest savedRequest =
                mock(KeyRecoveryRequest.class);

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(savedRequest.getId())
                .thenReturn(1L);

        when(savedRequest.getKeyId())
                .thenReturn(keyId);

        when(savedRequest.getRequestedBy())
                .thenReturn("admin@example.com");

        when(savedRequest.getReason())
                .thenReturn("Emergency key recovery");

        doReturn(getStatusConstant(savedRequest, "PENDING"))
                .when(savedRequest)
                .getStatus();

        when(savedRequest.getRequestedAt())
                .thenReturn(LocalDateTime.now());

        when(keyRecoveryService.createRecoveryRequest(
                eq(keyId),
                eq(recoveryRequest),
                eq("admin@example.com")
        )).thenReturn(savedRequest);

        ResponseEntity<?> response =
                controller.requestRecovery(
                        keyId,
                        recoveryRequest,
                        authentication
                );

        assertEquals(
                202,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(202, body.get("status"));
        assertEquals(
                "Emergency recovery request created",
                body.get("message")
        );
        assertEquals(1L, body.get("requestId"));
        assertEquals(keyId, body.get("keyId"));
        assertEquals(
                "admin@example.com",
                body.get("requestedBy")
        );
        assertEquals(
                "Emergency key recovery",
                body.get("reason")
        );

        verify(keyRecoveryService)
                .createRecoveryRequest(
                        keyId,
                        recoveryRequest,
                        "admin@example.com"
                );
    }

    @Test
    void requestRecoveryShouldReturnBadRequestForInvalidRequest() {

        String keyId = "INVALID-RECOVERY";

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.createRecoveryRequest(
                eq(keyId),
                eq(recoveryRequest),
                eq("admin@example.com")
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid recovery request"
                )
        );

        ResponseEntity<?> response =
                controller.requestRecovery(
                        keyId,
                        recoveryRequest,
                        authentication
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(400, body.get("status"));
        assertEquals(
                "Bad Request",
                body.get("error")
        );
        assertEquals(
                "Invalid recovery request",
                body.get("message")
        );
    }

    @Test
    void requestRecoveryShouldReturnConflictWhenRecoveryCannotBeCreated() {

        String keyId = "CONFLICT-RECOVERY";

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.createRecoveryRequest(
                eq(keyId),
                eq(recoveryRequest),
                eq("admin@example.com")
        )).thenThrow(
                new IllegalStateException(
                        "Recovery request already exists"
                )
        );

        ResponseEntity<?> response =
                controller.requestRecovery(
                        keyId,
                        recoveryRequest,
                        authentication
                );

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(409, body.get("status"));
        assertEquals(
                "Conflict",
                body.get("error")
        );
        assertEquals(
                "Recovery request already exists",
                body.get("message")
        );
    }

    // ---------------------------------------------------------
    // APPROVE RECOVERY
    // ---------------------------------------------------------

    @Test
    void approveRecoveryShouldReturnSuccess() {

        Long requestId = 1L;

        KeyRecoveryRequest approvedRequest =
                mock(KeyRecoveryRequest.class);

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(approvedRequest.getId())
                .thenReturn(requestId);

        when(approvedRequest.getKeyId())
                .thenReturn("PHASE17-RECOVERY-001");

        doReturn(getStatusConstant(approvedRequest, "APPROVED"))
                .when(approvedRequest)
                .getStatus();

        when(approvedRequest.getProcessedAt())
                .thenReturn(LocalDateTime.now());

        when(keyRecoveryService.approveRecoveryRequest(
                requestId,
                "admin@example.com"
        )).thenReturn(approvedRequest);

        ResponseEntity<?> response =
                controller.approveRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(200, body.get("status"));
        assertEquals(
                "Emergency recovery request approved",
                body.get("message")
        );
        assertEquals(requestId, body.get("requestId"));
        assertEquals(
                "PHASE17-RECOVERY-001",
                body.get("keyId")
        );
        assertEquals(
                "admin@example.com",
                body.get("approvedBy")
        );

        verify(keyRecoveryService)
                .approveRecoveryRequest(
                        requestId,
                        "admin@example.com"
                );
    }

    @Test
    void approveRecoveryShouldReturnBadRequestForInvalidRequestId() {

        Long requestId = 999L;

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.approveRecoveryRequest(
                requestId,
                "admin@example.com"
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid recovery request ID"
                )
        );

        ResponseEntity<?> response =
                controller.approveRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(400, body.get("status"));
        assertEquals(
                "Bad Request",
                body.get("error")
        );
        assertEquals(
                "Invalid recovery request ID",
                body.get("message")
        );
    }

    @Test
    void approveRecoveryShouldReturnConflictWhenNotPending() {

        Long requestId = 1L;

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.approveRecoveryRequest(
                requestId,
                "admin@example.com"
        )).thenThrow(
                new IllegalStateException(
                        "Recovery request is not pending: 1"
                )
        );

        ResponseEntity<?> response =
                controller.approveRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(409, body.get("status"));
        assertEquals(
                "Conflict",
                body.get("error")
        );
        assertEquals(
                "Recovery request is not pending: 1",
                body.get("message")
        );
    }

    // ---------------------------------------------------------
    // EXECUTE RECOVERY
    // ---------------------------------------------------------

    @Test
    void executeRecoveryShouldReturnSuccess() {

        Long requestId = 2L;

        KeyRecoveryRequest completedRequest =
                mock(KeyRecoveryRequest.class);

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(completedRequest.getId())
                .thenReturn(requestId);

        when(completedRequest.getKeyId())
                .thenReturn("PHASE17-RECOVERY-001");

        doReturn(getStatusConstant(completedRequest, "COMPLETED"))
                .when(completedRequest)
                .getStatus();

        when(completedRequest.getProcessedAt())
                .thenReturn(LocalDateTime.now());

        when(keyRecoveryService.executeRecovery(
                requestId,
                "admin@example.com"
        )).thenReturn(completedRequest);

        ResponseEntity<?> response =
                controller.executeRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(200, body.get("status"));
        assertEquals(
                "Emergency recovery executed successfully",
                body.get("message")
        );
        assertEquals(requestId, body.get("requestId"));
        assertEquals(
                "PHASE17-RECOVERY-001",
                body.get("keyId")
        );
        assertEquals(
                "admin@example.com",
                body.get("executedBy")
        );

        verify(keyRecoveryService)
                .executeRecovery(
                        requestId,
                        "admin@example.com"
                );
    }

    @Test
    void executeRecoveryShouldReturnBadRequestForInvalidRequestId() {

        Long requestId = 999L;

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.executeRecovery(
                requestId,
                "admin@example.com"
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid recovery request ID"
                )
        );

        ResponseEntity<?> response =
                controller.executeRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(400, body.get("status"));
        assertEquals(
                "Bad Request",
                body.get("error")
        );
        assertEquals(
                "Invalid recovery request ID",
                body.get("message")
        );
    }

    @Test
    void executeRecoveryShouldReturnConflictWhenNotApproved() {

        Long requestId = 1L;

        when(authentication.getName())
                .thenReturn("admin@example.com");

        when(keyRecoveryService.executeRecovery(
                requestId,
                "admin@example.com"
        )).thenThrow(
                new IllegalStateException(
                        "Recovery request must be APPROVED before execution: 1"
                )
        );

        ResponseEntity<?> response =
                controller.executeRecovery(
                        requestId,
                        authentication
                );

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        Map<String, Object> body =
                getBody(response);

        assertNotNull(body);

        assertEquals(409, body.get("status"));
        assertEquals(
                "Conflict",
                body.get("error")
        );
        assertEquals(
                "Recovery request must be APPROVED before execution: 1",
                body.get("message")
        );
    }

    // ---------------------------------------------------------
    // HELPER METHODS
    // ---------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBody(
            ResponseEntity<?> response
    ) {
        return (Map<String, Object>) response.getBody();
    }

    /**
     * Gets the recovery status enum without requiring the test
     * to hard-code the enum class name.
     */
    private Object getStatusConstant(
            KeyRecoveryRequest request,
            String constantName
    ) {

        try {

            Method method =
                    KeyRecoveryRequest.class
                            .getMethod("getStatus");

            Class<?> statusType =
                    method.getReturnType();

            if (!statusType.isEnum()) {
                throw new IllegalStateException(
                        "Recovery status is not an enum"
                );
            }

            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType =
                    (Class<? extends Enum>) statusType;

            return Enum.valueOf(
                    enumType,
                    constantName
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to resolve recovery status: "
                            + constantName,
                    exception
            );
        }
    }
}