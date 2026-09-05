package com.healthcare.keymanagement.service;

public interface KeyProtectionService {

    String protectKey(String key);

    String unprotectKey(String protectedKey);

    // Compatibility methods used by integration code
    default String protect(String key) {

        return protectKey(key);
    }

    default String unprotect(String protectedKey) {
        return unprotectKey(protectedKey);
    }
}