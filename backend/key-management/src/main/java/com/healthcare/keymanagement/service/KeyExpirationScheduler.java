package com.healthcare.keymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyExpirationScheduler {

    private final KeyExpirationService keyExpirationService;

    @Scheduled(
            fixedRateString = "${key.expiration.check-interval-ms:60000}"
    )
    public void checkForExpiredKeys() {

        int expiredCount = keyExpirationService.expireKeys();

        if (expiredCount > 0) {
            log.info(
                    "KEY EXPIRATION -> {} key(s) expired",
                    expiredCount
            );
        }
    }
}