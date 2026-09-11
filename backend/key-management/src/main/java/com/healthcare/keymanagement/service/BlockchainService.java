package com.healthcare.keymanagement.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;

    public String getBlockchainClientVersion() throws Exception {

        return web3j.web3ClientVersion()
                .send()
                .getWeb3ClientVersion();
    }
}