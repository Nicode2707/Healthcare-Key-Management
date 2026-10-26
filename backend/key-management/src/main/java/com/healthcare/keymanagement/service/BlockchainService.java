package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.blockchain.KeyLifecycleRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final Credentials blockchainCredentials;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    /**
     * Returns the connected blockchain client's version.
     */
    public String getBlockchainClientVersion() throws Exception {
        return web3j.web3ClientVersion()
                .send()
                .getWeb3ClientVersion();
    }

    /**
     * Reads the smart contract owner.
     */
    public String getContractOwner() throws Exception {
        KeyLifecycleRegistry contract = getContract();
        return contract.owner().send();
    }

    /**
     * Reads the number of lifecycle events recorded for a key.
     */
    public BigInteger getEventCount(String keyId) throws Exception {
        validateKeyId(keyId);

        KeyLifecycleRegistry contract = getContract();
        return contract.getEventCount(keyId).send();
    }

    /**
     * Reads one lifecycle event by its zero-based index.
     */
    public Map<String, Object> getKeyEvent(String keyId, BigInteger index)
            throws Exception {

        validateKeyId(keyId);

        if (index == null || index.signum() < 0) {
            throw new IllegalArgumentException(
                    "Event index must be zero or greater"
            );
        }

        KeyLifecycleRegistry contract = getContract();

        BigInteger eventCount = contract.getEventCount(keyId).send();

        if (index.compareTo(eventCount) >= 0) {
            throw new IllegalArgumentException(
                    "Event index is out of range. Event count for key "
                            + keyId + " is " + eventCount
            );
        }

        Tuple6<String, BigInteger, BigInteger, byte[], BigInteger, String>
                event = contract.getKeyEvent(keyId, index).send();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyId", event.component1());
        result.put("version", event.component2());
        result.put("eventType", event.component3());
        result.put("eventTypeName", getEventTypeName(event.component3()));
        result.put("recordHash", Numeric.toHexString(event.component4()));
        result.put("timestamp", event.component5());
        result.put("recordedBy", event.component6());

        return result;
    }

    /**
     * Reads the total number of lifecycle records stored on-chain.
     */
    public BigInteger getTotalRecords() throws Exception {
        KeyLifecycleRegistry contract = getContract();
        return contract.totalRecords().send();
    }

    /**
     * Preserves the existing lifecycle-event write operation.
     */
    public TransactionReceipt recordKeyLifecycleEvent(
            String keyId,
            int version,
            int eventType,
            String recordHash) {

        try {
            validateKeyId(keyId);

            if (version < 1) {
                throw new IllegalArgumentException(
                        "Version must be greater than zero"
                );
            }

            if (eventType < 0 || eventType > 5) {
                throw new IllegalArgumentException(
                        "Unsupported lifecycle event type"
                );
            }

            byte[] hashBytes = Numeric.hexStringToByteArray(recordHash);

            if (hashBytes.length != 32) {
                throw new IllegalArgumentException(
                        "Record hash must be exactly 32 bytes (bytes32)"
                );
            }

            KeyLifecycleRegistry contract = getContract();

            return contract.recordKeyEvent(
                    keyId,
                    BigInteger.valueOf(version),
                    BigInteger.valueOf(eventType),
                    hashBytes
            ).send();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to record lifecycle event on blockchain",
                    exception
            );
        }
    }

    /**
     * Creates a wrapper for the already-deployed smart contract.
     */
    private KeyLifecycleRegistry getContract() {
        return KeyLifecycleRegistry.load(
                contractAddress,
                web3j,
                blockchainCredentials,
                BigInteger.valueOf(20_000_000_000L),
                BigInteger.valueOf(5_000_000L)
        );
    }

    private void validateKeyId(String keyId) {
        if (keyId == null || keyId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Key ID must not be empty"
            );
        }
    }

    private String getEventTypeName(BigInteger eventType) {
        if (eventType == null) {
            return "UNKNOWN";
        }

        switch (eventType.intValue()) {
            case 0:
                return "CREATED";
            case 1:
                return "ROTATED";
            case 2:
                return "REVOKED";
            case 3:
                return "ARCHIVED";
            case 4:
                return "EXPIRED";
            case 5:
                return "RECOVERED";
            default:
                return "UNKNOWN";
        }
    }
}

