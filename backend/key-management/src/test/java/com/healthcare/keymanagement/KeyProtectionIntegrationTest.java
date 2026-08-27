package com.healthcare.keymanagement;

import com.healthcare.keymanagement.service.KeyProtectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KeyProtectionIntegrationTest {

    @Autowired
    private KeyProtectionService keyProtectionService;

    @Test
    void protectionServiceShouldBeAvailable() {

        assertNotNull(keyProtectionService);
    }

    @Test
    void shouldProtectAndRestoreKey() {

        String originalKey =
                "12345678901234567890123456789012";

        String protectedKey =
                keyProtectionService.protect(originalKey);

        String restoredKey =
                keyProtectionService.unprotect(protectedKey);

        assertNotEquals(originalKey, protectedKey);
        assertEquals(originalKey, restoredKey);
    }
}