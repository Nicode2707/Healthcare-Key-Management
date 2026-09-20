package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.blockchain.KeyLifecycleRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;

    private final Credentials blockchainCredentials;

    @Value("${blockchain.contract-address}")
    private String contractAddress;


    // =========================================================
    // BLOCKCHAIN CONNECTION
    // =========================================================

    /**
     * Tests the connection between Spring Boot and
     * the local Hardhat blockchain node.
     */
    public String getBlockchainClientVersion() throws Exception {

        return web3j.web3ClientVersion()
                .send()
                .getWeb3ClientVersion();
    }


    // =========================================================
    // CONTRACT OWNER
    // =========================================================

    /**
     * Reads the owner address directly from the
     * KeyLifecycleRegistry smart contract.
     *
     * Solidity function:
     * owner()
     *
     * Function selector:
     * 0x8da5cb5b
     */
    public String getContractOwner() throws Exception {

        String ownerFunctionSelector = "0x8da5cb5b";

        EthCall response =
                web3j.ethCall(
                        Transaction.createEthCallTransaction(
                                null,
                                contractAddress,
                                ownerFunctionSelector
                        ),
                        DefaultBlockParameterName.LATEST
                ).send();


        if (response.hasError()) {

            throw new IllegalStateException(
                    "Blockchain eth_call failed: "
                            + response.getError().getMessage()
            );
        }


        String result =
                response.getValue();


        if (result == null
                || result.equals("0x")
                || result.length() < 42) {

            throw new IllegalStateException(
                    "Empty or invalid owner response from contract"
            );
        }


        /*
         * ABI returns the Ethereum address
         * padded to 32 bytes.
         *
         * The last 40 hexadecimal characters
         * represent the actual 20-byte address.
         */
        String ownerAddress =
                "0x"
                        + result.substring(
                        result.length() - 40
                );


        return ownerAddress;
    }


    // =========================================================
    // RECORD KEY LIFECYCLE EVENT
    // =========================================================

    /**
     * Records a key lifecycle event on the blockchain.
     *
     * Event types:
     *
     * 0 = CREATED
     * 1 = ROTATED
     * 2 = REVOKED
     * 3 = ARCHIVED
     * 4 = EXPIRED
     * 5 = RECOVERED
     *
     * The actual AES key is NEVER sent to the blockchain.
     *
     * Only:
     *
     * keyId
     * version
     * eventType
     * recordHash
     *
     * are submitted.
     */
    public TransactionReceipt recordKeyLifecycleEvent(
            String keyId,
            int version,
            int eventType,
            String recordHash
    ) {

        try {

            // -------------------------------------------------
            // Load deployed smart contract
            // -------------------------------------------------

            KeyLifecycleRegistry contract =
                    KeyLifecycleRegistry.load(
                            contractAddress,
                            web3j,
                            blockchainCredentials,
                            BigInteger.valueOf(20_000_000_000L),
                            BigInteger.valueOf(5_000_000L)
                    );


            // -------------------------------------------------
            // Submit lifecycle event transaction
            // -------------------------------------------------

            return contract
                    .recordKeyEvent(
                            keyId,
                            BigInteger.valueOf(version),
                            BigInteger.valueOf(eventType),
                            Numeric.hexStringToByteArray(recordHash)
                    )
                    .send();

        } catch (Exception exception) {

            /*
             * Web3j throws checked exceptions while
             * communicating with the blockchain.
             *
             * Convert them into an application-level
             * runtime exception so callers such as
             * KeyMetadataService do not need to handle
             * checked blockchain exceptions.
             */
            throw new IllegalStateException(
                    "Failed to record lifecycle event on blockchain",
                    exception
            );
        }
    }
}