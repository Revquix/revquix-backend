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
package com.revquix.backend.notification.service;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: ZeptoMailService
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.InternalServerException;
import com.revquix.backend.notification.payload.ZeptoMailRequest;
import com.revquix.backend.notification.payload.ZeptoMailResponse;
import com.revquix.backend.notification.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZeptoMailService {

    private final WebClient webClient;
    private final MailProperties mailProperties;

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
    public ZeptoMailResponse send(String prefix, String to, String subject, String body) {
        try {
            MailProperties.ZeptoMail zeptoMail = mailProperties.getZeptoMail();
            ZeptoMailRequest request = buildEmailRequest(prefix, to, subject, body);
            ZeptoMailResponse response = webClient.post()
                    .uri(zeptoMail.getApiUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Zoho-enczapikey " + zeptoMail.getApiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ZeptoMailResponse.class)
                    .timeout(Duration.ofSeconds(zeptoMail.getTimeoutSeconds()))
                    .block();

            log.info("Email sent successfully. Request ID: {}",
                    response != null ? response.getRequestId() : "N/A");

            return response;
        } catch (WebClientResponseException exception) {
            log.error("ZeptoMail API error - Status: {}, Body: {}, Attempt will be retried if applicable",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw exception;
        } catch (Exception exception) {
            log.error("Error while sending email via ZeptoMail: {}", exception.getMessage());
            throw exception;
        }
    }

    @Recover
    public ZeptoMailResponse recover(Exception e, String prefix, String to, String subject, String body) {
        log.error("Failed to send email after all retry attempts. From: {}, To: {}, Subject: {}",
                prefix, to, subject, e);
        throw new InternalServerException(ErrorData.FAILED_TO_SEND_MAIL_API_ERROR, e);
    }

    private ZeptoMailRequest buildEmailRequest(String prefix, String toAddress, String subject, String htmlBody) {
        ZeptoMailRequest.From from = new ZeptoMailRequest.From(
                String.format("%s@revquix.com", prefix)
        );

        ZeptoMailRequest.EmailAddress emailAddress = new ZeptoMailRequest.EmailAddress(toAddress);
        ZeptoMailRequest.To to = new ZeptoMailRequest.To(emailAddress);

        return ZeptoMailRequest.builder()
                .from(from)
                .to(List.of(to))
                .subject(subject)
                .htmlBody(htmlBody)
                .build();
    }

}
