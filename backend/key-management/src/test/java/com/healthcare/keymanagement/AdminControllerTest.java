package com.healthcare.keymanagement;

import com.healthcare.keymanagement.controller.AdminController;
import com.healthcare.keymanagement.service.KeyMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminControllerTest {

    private KeyMetadataService keyMetadataService;
    private Authentication authentication;
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        keyMetadataService = mock(KeyMetadataService.class);
        authentication = mock(Authentication.class);

        adminController = new AdminController(keyMetadataService);
    }

    @Test
    void adminCheckShouldReturnAdminAccess() {

        when(authentication.getName())
                .thenReturn("admin@example.com");

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication)
                .getAuthorities();

        Map<String, Object> response =
                adminController.adminCheck(authentication);

        assertNotNull(response);
        assertEquals("Admin access granted", response.get("message"));
        assertEquals("admin@example.com", response.get("username"));
        assertEquals("ROLE_ADMIN", response.get("role"));
    }

    @Test
    void archiveKeyShouldReturnSuccess() {

        String keyId = "PHASE12-ARCHIVE-001";

        when(keyMetadataService.archiveKey(keyId))
                .thenReturn(2);

        Map<String, Object> response;

        try {
            var result = adminController.archiveKey(keyId);

            assertEquals(200, result.getStatusCode().value());

            response = (Map<String, Object>) result.getBody();

            assertNotNull(response);
            assertEquals(200, response.get("status"));
            assertEquals("Key archived successfully",
                    response.get("message"));
            assertEquals(keyId, response.get("keyId"));
            assertEquals(2, response.get("archivedKeys"));

        } catch (Exception exception) {
            fail("Archive operation should succeed", exception);
        }

        verify(keyMetadataService).archiveKey(keyId);
    }

    @Test
    void archiveKeyShouldReturnConflictWhenKeyCannotBeArchived() {

        String keyId = "PHASE12-ACTIVE-001";

        when(keyMetadataService.archiveKey(keyId))
                .thenThrow(new IllegalStateException(
                        "Active key cannot be archived"
                ));

        var result = adminController.archiveKey(keyId);

        assertEquals(409, result.getStatusCode().value());

        Map<String, Object> response =
                (Map<String, Object>) result.getBody();

        assertNotNull(response);
        assertEquals(409, response.get("status"));
        assertEquals("Conflict", response.get("error"));
        assertEquals(
                "Active key cannot be archived",
                response.get("message")
        );

        verify(keyMetadataService).archiveKey(keyId);
    }

    @Test
    void archiveKeyShouldPropagateKeyNotFoundException() {

        String keyId = "UNKNOWN-KEY";

        when(keyMetadataService.archiveKey(keyId))
                .thenThrow(new RuntimeException("Key not found"));

        assertThrows(
                RuntimeException.class,
                () -> adminController.archiveKey(keyId)
        );

        verify(keyMetadataService).archiveKey(keyId);
    }
}