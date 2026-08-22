package com.healthcare.keymanagement;

import com.healthcare.keymanagement.service.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionServiceImpl encryptionService;

    private String key;

    @BeforeEach
    void setUp() {

        encryptionService = new EncryptionServiceImpl();

        key = encryptionService.generateKey();
    }

    @Test
    void shouldGenerateValidAes256Key() {

        assertNotNull(key);
        assertFalse(key.isBlank());

        byte[] decodedKey =
                java.util.Base64.getDecoder().decode(key);

        assertEquals(32, decodedKey.length);
    }

    @Test
    void shouldEncryptAndDecryptSuccessfully() {

        String originalText =
                "Patient healthcare information";

        String encryptedText =
                encryptionService.encrypt(
                        originalText,
                        key
                );

        String decryptedText =
                encryptionService.decrypt(
                        encryptedText,
                        key
                );

        assertNotNull(encryptedText);
        assertNotEquals(originalText, encryptedText);
        assertEquals(originalText, decryptedText);
    }

    @Test
    void encryptionShouldProduceDifferentCiphertextEachTime() {

        String originalText =
                "Sensitive healthcare information";

        String encryptedText1 =
                encryptionService.encrypt(
                        originalText,
                        key
                );

        String encryptedText2 =
                encryptionService.encrypt(
                        originalText,
                        key
                );

        assertNotEquals(
                encryptedText1,
                encryptedText2
        );
    }

    @Test
    void shouldRejectInvalidKey() {

        String invalidKey =
                "invalid-key";

        assertThrows(
                IllegalArgumentException.class,
                () -> encryptionService.encrypt(
                        "Hello",
                        invalidKey
                )
        );
    }

    @Test
    void shouldFailWithWrongKey() {

        String originalText =
                "Sensitive healthcare information";

        String encryptedText =
                encryptionService.encrypt(
                        originalText,
                        key
                );

        String anotherKey =
                encryptionService.generateKey();

        assertThrows(
                IllegalStateException.class,
                () -> encryptionService.decrypt(
                        encryptedText,
                        anotherKey
                )
        );
    }

    @Test
    void shouldRejectEmptyPlainText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> encryptionService.encrypt(
                        "",
                        key
                )
        );
    }
}
