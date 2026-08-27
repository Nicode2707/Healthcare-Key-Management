package com.healthcare.keymanagement.service.impl;

import com.healthcare.keymanagement.service.KeyProtectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class KeyProtectionServiceImpl implements KeyProtectionService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";

    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SecretKey protectionKey;

    public KeyProtectionServiceImpl(
            @Value("${key.protection.secret}") String base64ProtectionKey) {

        this.protectionKey = loadProtectionKey(base64ProtectionKey);
    }

    @Override
    public String protectKey(String key) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Key cannot be null or empty"
            );
        }

        try {

            // Generate a new random IV for every encryption
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    protectionKey,
                    gcmParameterSpec
            );

            byte[] encryptedData =
                    cipher.doFinal(
                            key.getBytes(StandardCharsets.UTF_8)
                    );

            /*
             * Store:
             *
             * IV + encrypted data
             *
             * IV is not secret.
             */
            byte[] result =
                    new byte[IV_SIZE + encryptedData.length];

            System.arraycopy(
                    iv,
                    0,
                    result,
                    0,
                    IV_SIZE
            );

            System.arraycopy(
                    encryptedData,
                    0,
                    result,
                    IV_SIZE,
                    encryptedData.length
            );

            return Base64.getEncoder()
                    .encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to protect key",
                    e
            );
        }
    }

    @Override
    public String unprotectKey(String protectedKey) {

        if (protectedKey == null || protectedKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Protected key cannot be null or empty"
            );
        }

        try {

            byte[] combinedData =
                    Base64.getDecoder()
                            .decode(protectedKey);

            if (combinedData.length <= IV_SIZE) {
                throw new IllegalArgumentException(
                        "Invalid protected key"
                );
            }

            // Extract IV
            byte[] iv = new byte[IV_SIZE];

            System.arraycopy(
                    combinedData,
                    0,
                    iv,
                    0,
                    IV_SIZE
            );

            // Extract encrypted data
            byte[] encryptedData =
                    new byte[combinedData.length - IV_SIZE];

            System.arraycopy(
                    combinedData,
                    IV_SIZE,
                    encryptedData,
                    0,
                    encryptedData.length
            );

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            Cipher cipher =
                    Cipher.getInstance(AES_GCM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    protectionKey,
                    gcmParameterSpec
            );

            byte[] decryptedData =
                    cipher.doFinal(encryptedData);

            return new String(
                    decryptedData,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to unprotect key",
                    e
            );
        }
    }

    private SecretKey loadProtectionKey(
            String base64ProtectionKey) {

        if (base64ProtectionKey == null ||
                base64ProtectionKey.isBlank()) {

            throw new IllegalArgumentException(
                    "key.protection.secret must not be empty"
            );
        }

        try {

            byte[] keyBytes =
                    Base64.getDecoder()
                            .decode(base64ProtectionKey);

            if (keyBytes.length * 8 != KEY_SIZE) {

                throw new IllegalArgumentException(
                        "Protection key must be exactly 256 bits"
                );
            }

            return new SecretKeySpec(
                    keyBytes,
                    AES
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid Base64 protection key. " +
                            "It must contain exactly 32 bytes (256 bits).",
                    e
            );
        }
    }
}