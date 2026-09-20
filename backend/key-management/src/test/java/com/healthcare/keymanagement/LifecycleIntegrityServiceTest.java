package com.healthcare.keymanagement;

import com.healthcare.keymanagement.service.LifecycleIntegrityService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleIntegrityServiceTest {

    private final LifecycleIntegrityService service =
            new LifecycleIntegrityService();

    @Test
    void shouldGenerateDeterministicHash() {

        String hash1 =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "CREATED"
                );

        String hash2 =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "CREATED"
                );

        assertNotNull(hash1);
        assertNotNull(hash2);

        assertEquals(hash1, hash2);
    }

    @Test
    void shouldGenerateDifferentHashForDifferentEvent() {

        String createdHash =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "CREATED"
                );

        String revokedHash =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "REVOKED"
                );

        assertNotEquals(createdHash, revokedHash);
    }

    @Test
    void shouldGenerateDifferentHashForDifferentVersion() {

        String versionOneHash =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "CREATED"
                );

        String versionTwoHash =
                service.generateRecordHash(
                        "HKM-001",
                        2,
                        "CREATED"
                );

        assertNotEquals(versionOneHash, versionTwoHash);
    }

    @Test
    void shouldGenerateValidKeccak256HashFormat() {

        String hash =
                service.generateRecordHash(
                        "HKM-001",
                        1,
                        "CREATED"
                );

        assertNotNull(hash);

        // Keccak-256 = 32 bytes = 64 hexadecimal characters
        // + "0x" prefix = 66 characters
        assertTrue(hash.startsWith("0x"));
        assertEquals(66, hash.length());
    }
}
