/**
 * Proprietary License Agreement
 * <p>
 * Copyright (c) 2025 Revquix
 * <p>
 * This software is the confidential and proprietary property of Revquix and is provided under a
 * license, not sold. The application owner is Rohit Parihar and Revquix. Only authorized
 * Revquix administrators are permitted to copy, modify, distribute, or sublicense this software
 * under the terms set forth in this agreement.
 * <p>
 * Restrictions
 *
 * You are expressly prohibited from:
 * 1. Copying, modifying, distributing, or sublicensing this software without the express
 *    written permission of Rohit Parihar or Revquix.
 * 2. Reverse engineering, decompiling, disassembling, or otherwise attempting to derive
 *    the source code of the software.
 * 3. Altering or modifying the terms of this license without prior written approval from
 *    Rohit Parihar and Revquix administrators.
 * <p>
 * Disclaimer of Warranties:
 * This software is provided "as is" without any warranties, express or implied. Revquix makes
 * no representations or warranties regarding the software, including but not limited to any
 * warranties of merchantability, fitness for a particular purpose, or non-infringement.
 * <p>
 * For inquiries regarding licensing, please contact: support@Revquix.com.
 */
package com.revquix.backend.application.service;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: IpService
 */

import com.revquix.backend.application.payload.IPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpService {

    private final WebClient webClient;

    @Retryable(
            retryFor = {
                    WebClientResponseException.class,
                    ConnectException.class,
                    TimeoutException.class,
                    WebClientRequestException.class
            },
            noRetryFor = {
                    WebClientResponseException.BadRequest.class,
                    WebClientResponseException.Unauthorized.class,
                    WebClientResponseException.Forbidden.class
            },
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1000, // 1000, 2*1000 = 2000, 2*2000 = 4000, 2*4000 = 8000 (capped to maxDelay)
                    multiplier = 2,
                    maxDelay = 6000
            )
    )
    public IPResponse getIpDetails(String ipAddress) {
        log.info("{}::getIpDetails -> Fetching details for IP address: {}", getClass().getSimpleName(), ipAddress);
        try {
            IPResponse response = webClient
                    .get()
                    .uri("http://ip-api.com/json/{ip}", ipAddress)
                    .retrieve()
                    .bodyToMono(IPResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            log.info("{}::getIpDetails -> IP address details: {}", getClass().getSimpleName(), response.toJson());
            return response;
        } catch (WebClientResponseException exception) {
            log.error("{}::getIpDetails -> WebClientResponseException- Status: {}, Body: {}, Attempt will be retried if applicable", getClass().getSimpleName(), exception.getStatusCode(), exception.getResponseBodyAsString(), exception.getResponseBodyAsString());
            throw exception;
        } catch (Exception exception) {
            log.error("{}::getIpDetails -> Exception occurred while fetching IP details: {}", getClass().getSimpleName(), exception.getMessage(), exception);
            throw exception;
        }
    }
}
