package com.healthcare.keymanagement;

import com.healthcare.keymanagement.service.AesKeyService;
import com.healthcare.keymanagement.service.impl.KeyProtectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyProtectionServiceTest {

    private KeyProtectionServiceImpl service;

    private String protectionKey;

    @BeforeEach
    void setUp() {

        protectionKey =
                new AesKeyService()
                        .generateAes256Key();

        service = new KeyProtectionServiceImpl(
                protectionKey
        );
    }

    @Test
    void shouldProtectAndUnprotectKey() {

        String originalKey =
                new AesKeyService()
                        .generateAes256Key();

        String protectedKey =
                service.protectKey(originalKey);

        String restoredKey =
                service.unprotectKey(protectedKey);

        assertNotNull(protectedKey);
        assertNotEquals(originalKey, protectedKey);
        assertEquals(originalKey, restoredKey);
    }

    @Test
    void protectingSameKeyTwiceShouldProduceDifferentValues() {

        String originalKey =
                new AesKeyService()
                        .generateAes256Key();

        String protectedKey1 =
                service.protectKey(originalKey);

        String protectedKey2 =
                service.protectKey(originalKey);

        assertNotEquals(
                protectedKey1,
                protectedKey2
        );
    }

    @Test
    void wrongProtectionKeyShouldFail() {

        String originalKey =
                new AesKeyService()
                        .generateAes256Key();

        String protectedKey =
                service.protectKey(originalKey);

        String anotherProtectionKey =
                new AesKeyService()
                        .generateAes256Key();

        KeyProtectionServiceImpl anotherService =
                new KeyProtectionServiceImpl(
                        anotherProtectionKey
                );

        assertThrows(
                IllegalStateException.class,
                () -> anotherService.unprotectKey(
                        protectedKey
                )
        );
    }

    @Test
    void shouldRejectEmptyKey() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.protectKey("")
        );
    }
}