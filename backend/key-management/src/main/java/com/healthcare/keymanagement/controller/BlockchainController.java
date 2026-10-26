package com.healthcare.keymanagement.controller;

import com.healthcare.keymanagement.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/connection")
    public String testConnection() throws Exception {
        return blockchainService.getBlockchainClientVersion();
    }

    @GetMapping("/owner")
    public String getContractOwner() throws Exception {
        return blockchainService.getContractOwner();
    }

    @GetMapping("/records/count")
    public Map<String, Object> getTotalRecords() throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalRecords", blockchainService.getTotalRecords());
        return response;
    }

    @GetMapping("/keys/{keyId}/events/count")
    public Map<String, Object> getEventCount(
            @PathVariable String keyId) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyId", keyId);
        response.put(
                "eventCount",
                blockchainService.getEventCount(keyId)
        );

        return response;
    }

    @GetMapping("/keys/{keyId}/events/{index}")
    public Map<String, Object> getKeyEvent(
            @PathVariable String keyId,
            @PathVariable String index) throws Exception {

        BigInteger eventIndex;

        try {
            eventIndex = new BigInteger(index);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Event index must be a non-negative integer"
            );
        }

        return blockchainService.getKeyEvent(keyId, eventIndex);
    }
}

