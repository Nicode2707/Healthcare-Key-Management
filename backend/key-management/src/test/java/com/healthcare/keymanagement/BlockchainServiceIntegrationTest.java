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

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlockchainServiceIntegrationTest {

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
    // COMPLETE BLOCKCHAIN LIFECYCLE TEST
    // =========================================================

    @Test
    void shouldRecordCompleteLifecycleOnBlockchain()
            throws Exception {

        // ---------------------------------------------------------
        // Unique test key
        // ---------------------------------------------------------

        String keyId =
                "PHASE23-LIFECYCLE-" + System.currentTimeMillis();


        // ---------------------------------------------------------
        // Load deployed smart contract
        // ---------------------------------------------------------

        KeyLifecycleRegistry blockchainContract =
                KeyLifecycleRegistry.load(
                        contractAddress,
                        web3j,
                        blockchainCredentials,
                        BigInteger.valueOf(20_000_000_000L),
                        BigInteger.valueOf(5_000_000L)
                );


        // ---------------------------------------------------------
        // Initial event count
        // ---------------------------------------------------------

        BigInteger initialEventCount =
                blockchainContract
                        .getEventCount(keyId)
                        .send();

        assertEquals(
                BigInteger.ZERO,
                initialEventCount,
                "New test key should have no lifecycle events"
        );


        // ---------------------------------------------------------
        // Define complete lifecycle
        // ---------------------------------------------------------

        List<LifecycleTestEvent> lifecycleEvents =
                List.of(

                        new LifecycleTestEvent(
                                1,
                                "CREATED",
                                0
                        ),

                        new LifecycleTestEvent(
                                2,
                                "ROTATED",
                                1
                        ),

                        new LifecycleTestEvent(
                                3,
                                "REVOKED",
                                2
                        ),

                        new LifecycleTestEvent(
                                3,
                                "ARCHIVED",
                                3
                        ),

                        new LifecycleTestEvent(
                                3,
                                "EXPIRED",
                                4
                        ),

                        new LifecycleTestEvent(
                                3,
                                "RECOVERED",
                                5
                        )
                );


        // =========================================================
        // RECORD EACH LIFECYCLE EVENT
        // =========================================================

        for (LifecycleTestEvent lifecycleEvent :
                lifecycleEvents) {

            // -----------------------------------------------------
            // Generate deterministic hash
            // -----------------------------------------------------

            String recordHash =
                    lifecycleIntegrityService.generateRecordHash(
                            keyId,
                            lifecycleEvent.version(),
                            lifecycleEvent.eventName()
                    );


            // -----------------------------------------------------
            // Record event on blockchain
            // -----------------------------------------------------

            TransactionReceipt receipt =
                    blockchainService.recordKeyLifecycleEvent(
                            keyId,
                            lifecycleEvent.version(),
                            lifecycleEvent.eventType(),
                            recordHash
                    );


            // -----------------------------------------------------
            // Verify transaction
            // -----------------------------------------------------

            assertNotNull(
                    receipt,
                    "Transaction receipt must not be null"
            );

            assertTrue(
                    receipt.isStatusOK(),
                    "Blockchain transaction should succeed for "
                            + lifecycleEvent.eventName()
            );


            // -----------------------------------------------------
            // Read event count
            // -----------------------------------------------------

            BigInteger eventCount =
                    blockchainContract
                            .getEventCount(keyId)
                            .send();


            assertTrue(
                    eventCount.compareTo(BigInteger.ONE) >= 0,
                    "Lifecycle event should exist on blockchain"
            );


            // -----------------------------------------------------
            // Read latest event
            // -----------------------------------------------------

            BigInteger latestIndex =
                    eventCount.subtract(BigInteger.ONE);


            var lifecycleEventData =
                    blockchainContract
                            .getKeyEvent(
                                    keyId,
                                    latestIndex
                            )
                            .send();


            // -----------------------------------------------------
            // Verify Key ID
            // -----------------------------------------------------

            assertEquals(
                    keyId,
                    lifecycleEventData.component1(),
                    "Blockchain key ID mismatch"
            );


            // -----------------------------------------------------
            // Verify Version
            // -----------------------------------------------------

            assertEquals(
                    BigInteger.valueOf(
                            lifecycleEvent.version()
                    ),
                    lifecycleEventData.component2(),
                    "Blockchain version mismatch"
            );


            // -----------------------------------------------------
            // Verify Event Type
            // -----------------------------------------------------

            assertEquals(
                    BigInteger.valueOf(
                            lifecycleEvent.eventType()
                    ),
                    lifecycleEventData.component3(),
                    "Blockchain event type mismatch"
            );


            // -----------------------------------------------------
            // Verify Record Hash
            // -----------------------------------------------------

            byte[] blockchainHash =
                    lifecycleEventData.component4();


            String blockchainHashHex =
                    org.web3j.utils.Numeric.toHexString(
                            blockchainHash
                    );


            assertEquals(
                    recordHash,
                    blockchainHashHex,
                    "Blockchain record hash mismatch"
            );


            // -----------------------------------------------------
            // Verify transaction hash
            // -----------------------------------------------------

            assertNotNull(
                    receipt.getTransactionHash(),
                    "Transaction hash must exist"
            );


            // -----------------------------------------------------
            // Console verification
            // -----------------------------------------------------

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Lifecycle Event : "
                            + lifecycleEvent.eventName()
            );

            System.out.println(
                    "Key ID          : "
                            + keyId
            );

            System.out.println(
                    "Version         : "
                            + lifecycleEvent.version()
            );

            System.out.println(
                    "Event Type      : "
                            + lifecycleEvent.eventType()
            );

            System.out.println(
                    "Record Hash     : "
                            + recordHash
            );

            System.out.println(
                    "TX Hash         : "
                            + receipt.getTransactionHash()
            );
        }


        // =========================================================
        // FINAL EVENT COUNT VERIFICATION
        // =========================================================

        BigInteger finalEventCount =
                blockchainContract
                        .getEventCount(keyId)
                        .send();


        assertEquals(
                BigInteger.valueOf(
                        lifecycleEvents.size()
                ),
                finalEventCount,
                "Blockchain should contain all lifecycle events"
        );


        // =========================================================
        // FINAL RESULT
        // =========================================================

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "COMPLETE BLOCKCHAIN LIFECYCLE TEST PASSED"
        );

        System.out.println(
                "Key ID          : " + keyId
        );

        System.out.println(
                "Lifecycle Events: " + finalEventCount
        );

        System.out.println(
                "CREATED         : PASSED"
        );

        System.out.println(
                "ROTATED         : PASSED"
        );

        System.out.println(
                "REVOKED         : PASSED"
        );

        System.out.println(
                "ARCHIVED        : PASSED"
        );

        System.out.println(
                "EXPIRED         : PASSED"
        );

        System.out.println(
                "RECOVERED       : PASSED"
        );

        System.out.println(
                "=============================================="
        );
    }


    // =========================================================
    // TEST EVENT RECORD
    // =========================================================

    private record LifecycleTestEvent(
            int version,
            String eventName,
            int eventType
    ) {
    }
}