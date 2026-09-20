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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlockchainIntegrityVerificationTest {

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


    // =========================================================
    // TAMPER / INTEGRITY VERIFICATION
    // =========================================================

    @Test
    void shouldDetectLifecycleDataTampering()
            throws Exception {

        // -----------------------------------------------------
        // 1. Original lifecycle data
        // -----------------------------------------------------

        String keyId =
                "PHASE23-TAMPER-"
                        + System.currentTimeMillis();

        int originalVersion = 1;

        String originalEventType = "CREATED";

        int blockchainEventType = 0;


        // -----------------------------------------------------
        // 2. Generate original integrity hash
        // -----------------------------------------------------

        String originalHash =
                lifecycleIntegrityService.generateRecordHash(
                        keyId,
                        originalVersion,
                        originalEventType
                );


        // -----------------------------------------------------
        // 3. Store original evidence on blockchain
        // -----------------------------------------------------

        TransactionReceipt receipt =
                blockchainService.recordKeyLifecycleEvent(
                        keyId,
                        originalVersion,
                        blockchainEventType,
                        originalHash
                );


        assertNotNull(receipt);

        assertTrue(
                receipt.isStatusOK(),
                "Blockchain transaction should succeed"
        );


        // -----------------------------------------------------
        // 4. Load smart contract
        // -----------------------------------------------------

        KeyLifecycleRegistry blockchainContract =
                KeyLifecycleRegistry.load(
                        contractAddress,
                        web3j,
                        blockchainCredentials,
                        BigInteger.valueOf(
                                20_000_000_000L
                        ),
                        BigInteger.valueOf(
                                5_000_000L
                        )
                );


        // -----------------------------------------------------
        // 5. Read blockchain record
        // -----------------------------------------------------

        BigInteger eventCount =
                blockchainContract
                        .getEventCount(keyId)
                        .send();


        assertEquals(
                BigInteger.ONE,
                eventCount,
                "Exactly one test lifecycle event should exist"
        );


        var blockchainRecord =
                blockchainContract
                        .getKeyEvent(
                                keyId,
                                BigInteger.ZERO
                        )
                        .send();


        byte[] storedHashBytes =
                blockchainRecord.component4();


        String storedBlockchainHash =
                Numeric.toHexString(
                        storedHashBytes
                );


        // -----------------------------------------------------
        // 6. ORIGINAL DATA VERIFICATION
        // -----------------------------------------------------

        String regeneratedOriginalHash =
                lifecycleIntegrityService.generateRecordHash(
                        keyId,
                        originalVersion,
                        originalEventType
                );


        boolean originalDataMatches =
                regeneratedOriginalHash.equals(
                        storedBlockchainHash
                );


        assertTrue(
                originalDataMatches,
                "Original lifecycle data must match blockchain evidence"
        );


        // -----------------------------------------------------
        // 7. SIMULATE TAMPERING
        // -----------------------------------------------------

        /*
         * We intentionally change the lifecycle data.
         *
         * Blockchain still contains:
         *
         * Version 1
         * CREATED
         *
         * Tampered data:
         *
         * Version 2
         * CREATED
         */

        int tamperedVersion = 2;


        String tamperedHash =
                lifecycleIntegrityService.generateRecordHash(
                        keyId,
                        tamperedVersion,
                        originalEventType
                );


        // -----------------------------------------------------
        // 8. Compare tampered hash with blockchain hash
        // -----------------------------------------------------

        boolean tamperedDataMatches =
                tamperedHash.equals(
                        storedBlockchainHash
                );


        // -----------------------------------------------------
        // 9. Tampering MUST be detected
        // -----------------------------------------------------

        assertFalse(
                tamperedDataMatches,
                "Modified lifecycle data must not match blockchain evidence"
        );


        // =====================================================
        // FINAL VERIFICATION OUTPUT
        // =====================================================

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "BLOCKCHAIN INTEGRITY VERIFICATION PASSED"
        );

        System.out.println(
                "Key ID              : " + keyId
        );

        System.out.println(
                "Original Version    : " + originalVersion
        );

        System.out.println(
                "Original Event      : " + originalEventType
        );

        System.out.println(
                "Original Hash       : " + originalHash
        );

        System.out.println(
                "Blockchain Hash     : "
                        + storedBlockchainHash
        );

        System.out.println(
                "Original Data Match : "
                        + originalDataMatches
        );

        System.out.println(
                "Tampered Version   : "
                        + tamperedVersion
        );

        System.out.println(
                "Tampered Hash      : "
                        + tamperedHash
        );

        System.out.println(
                "Tampering Detected : "
                        + !tamperedDataMatches
        );

        System.out.println(
                "TX Hash             : "
                        + receipt.getTransactionHash()
        );

        System.out.println(
                "=============================================="
        );
    }
}