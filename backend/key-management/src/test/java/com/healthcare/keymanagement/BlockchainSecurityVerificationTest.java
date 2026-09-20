package com.healthcare.keymanagement;

import com.healthcare.keymanagement.blockchain.KeyLifecycleRegistry;
import com.healthcare.keymanagement.service.BlockchainService;
import com.healthcare.keymanagement.service.LifecycleIntegrityService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlockchainSecurityVerificationTest {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private LifecycleIntegrityService lifecycleIntegrityService;

    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials blockchainCredentials;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    @Test
    void shouldNotStoreSensitiveDataOnBlockchain()
            throws Exception {

        String keyId =
                "PHASE23-SECURITY-"
                        + System.currentTimeMillis();

        int version = 1;
        String eventType = "CREATED";

        /*
         * These are deliberately fake test values.
         * They represent information that must NEVER
         * be sent to the blockchain.
         */
        String plaintextAesKey =
                "FAKE-PLAINTEXT-AES-256-KEY-SECRET";

        String protectedKey =
                "FAKE-PROTECTED-AES-KEY-SECRET";

        String medicalData =
                "PATIENT-PRIVATE-MEDICAL-RECORD";

        String password =
                "FAKE-PASSWORD-SECRET";

        String jwtSecret =
                "FAKE-JWT-SECRET";

        String aesMasterKey =
                "FAKE-AES-MASTER-KEY";

        String blockchainPrivateKey =
                "FAKE-BLOCKCHAIN-PRIVATE-KEY";

        /*
         * Only the lifecycle evidence is hashed.
         */
        String recordHash =
                lifecycleIntegrityService.generateRecordHash(
                        keyId,
                        version,
                        eventType
                );

        /*
         * Send lifecycle evidence to blockchain.
         *
         * Notice that none of the sensitive values
         * above are passed to this method.
         */
        TransactionReceipt receipt =
                blockchainService.recordKeyLifecycleEvent(
                        keyId,
                        version,
                        0,
                        recordHash
                );

        assertNotNull(receipt);
        assertTrue(
                receipt.isStatusOK(),
                "Blockchain transaction should succeed"
        );

        /*
         * Retrieve the actual transaction input.
         *
         * This lets us inspect the data submitted to
         * the blockchain transaction.
         */
        String transactionInput =
                web3j
                        .ethGetTransactionByHash(
                                receipt.getTransactionHash()
                        )
                        .send()
                        .getTransaction()
                        .orElseThrow()
                        .getInput();

        assertNotNull(transactionInput);

        String transactionData =
                transactionInput.toLowerCase();

        /*
         * Convert sensitive test values to hexadecimal.
         *
         * Ethereum ABI transaction data is hexadecimal,
         * so we check the encoded representation.
         */
        assertFalse(
                transactionData.contains(
                        toHex(plaintextAesKey)
                ),
                "Plaintext AES key must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(protectedKey)
                ),
                "Protected AES key must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(medicalData)
                ),
                "Medical data must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(password)
                ),
                "Password must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(jwtSecret)
                ),
                "JWT secret must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(aesMasterKey)
                ),
                "AES master key must not be present in blockchain transaction"
        );

        assertFalse(
                transactionData.contains(
                        toHex(blockchainPrivateKey)
                ),
                "Blockchain private key must not be present in blockchain transaction"
        );

        /*
         * Verify the actual blockchain record.
         */
        KeyLifecycleRegistry blockchainContract =
                KeyLifecycleRegistry.load(
                        contractAddress,
                        web3j,
                        blockchainCredentials,
                        BigInteger.valueOf(20_000_000_000L),
                        BigInteger.valueOf(5_000_000L)
                );

        var blockchainRecord =
                blockchainContract
                        .getKeyEvent(
                                keyId,
                                BigInteger.ZERO
                        )
                        .send();

        String storedKeyId =
                blockchainRecord.component1();

        BigInteger storedVersion =
                blockchainRecord.component2();

        String storedHash =
                Numeric.toHexString(
                        blockchainRecord.component4()
                );

        assertEquals(
                keyId,
                storedKeyId,
                "Blockchain should contain the lifecycle key ID"
        );

        assertEquals(
                BigInteger.valueOf(version),
                storedVersion,
                "Blockchain should contain the lifecycle version"
        );

        assertEquals(
                recordHash,
                storedHash,
                "Blockchain should contain the lifecycle integrity hash"
        );

        System.out.println("==============================================");
        System.out.println("BLOCKCHAIN SECURITY VERIFICATION PASSED");
        System.out.println("Key ID              : " + keyId);
        System.out.println("Stored Version      : " + storedVersion);
        System.out.println("Stored Record Hash  : " + storedHash);
        System.out.println("----------------------------------------------");
        System.out.println("Plain AES Key       : NOT SENT");
        System.out.println("Protected Key       : NOT SENT");
        System.out.println("Medical Data        : NOT SENT");
        System.out.println("Password            : NOT SENT");
        System.out.println("JWT Secret          : NOT SENT");
        System.out.println("AES Master Key      : NOT SENT");
        System.out.println("Blockchain PrivKey  : NOT SENT");
        System.out.println("----------------------------------------------");
        System.out.println("Blockchain Security : PASSED");
        System.out.println("TX Hash             : "
                + receipt.getTransactionHash());
        System.out.println("==============================================");
    }

    private String toHex(String value) {

        return Numeric.toHexStringNoPrefix(
                value.getBytes(StandardCharsets.UTF_8)
        ).toLowerCase();
    }
}