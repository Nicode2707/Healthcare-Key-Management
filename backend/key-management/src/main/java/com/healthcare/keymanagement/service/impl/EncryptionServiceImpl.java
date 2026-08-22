package com.healthcare.keymanagement.service.impl;

import com.healthcare.keymanagement.service.EncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateKey() {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(AES_KEY_SIZE);

            SecretKey secretKey = keyGenerator.generateKey();

            return Base64.getEncoder()
                    .encodeToString(secretKey.getEncoded());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to generate AES-256 encryption key", e
            );
        }
    }

    @Override
    public String encrypt(String plainText, String base64Key) {

        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Plain text cannot be empty");
        }

        SecretKey secretKey = decodeKey(base64Key);

        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(TAG_LENGTH , iv);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmParameterSpec
            );

            byte[] encryptedBytes =
                    cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_SIZE + encryptedBytes.length];

            System.arraycopy(
                    iv,
                    0,
                    combined,
                    0,
                    IV_SIZE
            );

            System.arraycopy(
                    encryptedBytes,
                    0,
                    combined,
                    IV_SIZE,
                    encryptedBytes.length
            );

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to encrypt data", e
            );
        }
    }

    @Override
    public String decrypt(String encryptedText, String base64Key) {

        if (encryptedText == null || encryptedText.isBlank()) {
            throw new IllegalArgumentException(
                    "Encrypted text cannot be empty"
            );
        }

        SecretKey secretKey = decodeKey(base64Key);

        try {
            byte[] combined =
                    Base64.getDecoder().decode(encryptedText);

            if (combined.length <= IV_SIZE) {
                throw new IllegalArgumentException(
                        "Invalid encrypted data"
                );
            }

            byte[] iv = new byte[IV_SIZE];

            byte[] encryptedBytes =
                    new byte[combined.length - IV_SIZE];

            System.arraycopy(
                    combined,
                    0,
                    iv,
                    0,
                    IV_SIZE
            );

            System.arraycopy(
                    combined,
                    IV_SIZE,
                    encryptedBytes,
                    0,
                    encryptedBytes.length
            );

            Cipher cipher =
                    Cipher.getInstance(AES_GCM_NO_PADDING);

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(TAG_LENGTH , iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmParameterSpec
            );

            byte[] decryptedBytes =
                    cipher.doFinal(encryptedBytes);

            return new String(
                    decryptedBytes,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to decrypt data", e
            );
        }
    }

    private SecretKey decodeKey(String base64Key) {

        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException(
                    "Encryption key cannot be empty"
            );
        }

        try {

            byte[] keyBytes =
                    Base64.getDecoder().decode(base64Key);

            if (keyBytes.length != 32) {
                throw new IllegalArgumentException(
                        "AES-256 key must contain exactly 32 bytes"
                );
            }

            return new SecretKeySpec(keyBytes, AES);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid AES-256 Base64 key", e
            );
        }
    }
}