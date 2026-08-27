package com.healthcare.keymanagement.service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class AesKeyService {

    public String generateAes256Key() {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

            keyGenerator.init(256);

            SecretKey secretKey = keyGenerator.generateKey();

            return Base64.getEncoder()
                    .encodeToString(secretKey.getEncoded());

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "AES algorithm is not available", e
            );
        }
    }
}