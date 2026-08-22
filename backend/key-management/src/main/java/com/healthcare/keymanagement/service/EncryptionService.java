package com.healthcare.keymanagement.service;

public interface EncryptionService {

    String generateKey();

    String encrypt(String plainText, String base64Key);

    String decrypt(String encryptedText, String base64Key);
}