package com.healthcare.keymanagement;

import com.healthcare.keymanagement.service.AesKeyService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesKeyServiceTest {

    private final AesKeyService aesKeyService = new AesKeyService();

    @Test
    void shouldGenerateAes256Key() {

        String key = aesKeyService.generateAes256Key();

        assertNotNull(key);
        assertFalse(key.isBlank());

        byte[] decodedKey = java.util.Base64
                .getDecoder()
                .decode(key);

        assertEquals(32, decodedKey.length);
    }

    @Test
    void shouldGenerateDifferentKeys() {

        String key1 = aesKeyService.generateAes256Key();
        String key2 = aesKeyService.generateAes256Key();

        assertNotEquals(key1, key2);
    }
}