package com.healthcare.keymanagement.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    /**
     * Tests the connection between Spring Boot and
     * the local Hardhat blockchain node.
     */
    public String getBlockchainClientVersion() throws Exception {

        return web3j.web3ClientVersion()
                .send()
                .getWeb3ClientVersion();
    }

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

        String result = response.getValue();

        System.out.println(
                "BLOCKCHAIN DEBUG -> Contract Address: "
                        + contractAddress
        );

        System.out.println(
                "BLOCKCHAIN DEBUG -> Raw owner() response: "
                        + result
        );

        if (result == null
                || result.equals("0x")
                || result.length() < 42) {

            throw new IllegalStateException(
                    "Empty or invalid owner response from contract"
            );
        }

        /*
         * ABI returns an address padded to 32 bytes.
         *
         * Example:
         *
         * 0x000000000000000000000000f39fd6e51aad88f6f4ce6a...
         *
         * The last 40 hexadecimal characters represent
         * the actual 20-byte Ethereum address.
         */
        String ownerAddress =
                "0x"
                        + result.substring(
                        result.length() - 40
                );

        System.out.println(
                "BLOCKCHAIN DEBUG -> Contract Owner: "
                        + ownerAddress
        );

        return ownerAddress;
    }
}