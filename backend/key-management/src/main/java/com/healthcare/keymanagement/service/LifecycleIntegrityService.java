package com.healthcare.keymanagement.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

@Service
public class LifecycleIntegrityService {

    /**
     * Generates a deterministic integrity hash for
     * a key lifecycle event.
     *
     * Canonical format:
     *
     * keyId|version|eventType
     *
     * Example:
     * HKM-001|1|CREATED
     */
    public String generateRecordHash(
            String keyId,
            int version,
            String eventType
    ) {

        String canonicalData =
                keyId + "|" + version + "|" + eventType;

        byte[] data =
                canonicalData.getBytes(StandardCharsets.UTF_8);

        byte[] hash =
                Hash.sha3(data);

        return Numeric.toHexString(hash);
    }
}