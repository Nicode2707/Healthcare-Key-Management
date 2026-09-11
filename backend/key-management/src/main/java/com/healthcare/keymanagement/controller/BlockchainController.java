package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.service.BlockchainService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/connection")
    public String testConnection() throws Exception {

        return blockchainService.getBlockchainClientVersion();
    }
}