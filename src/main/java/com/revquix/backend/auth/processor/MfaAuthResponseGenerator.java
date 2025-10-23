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
package com.revquix.backend.auth.processor;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: MfaAuthResponseGenerator
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.application.utils.ServletUtil;
import com.revquix.backend.auth.dao.repository.MfaEntityRepository;
import com.revquix.backend.auth.model.MfaEntity;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import com.revquix.backend.auth.util.MfaTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaAuthResponseGenerator {

    private final AuthenticationProperties authenticationProperties;
    private final IpUtils ipUtils;
    private final ServletUtil servletUtil;
    private final MfaEntityRepository mfaEntityRepository;

    public AuthResponse generate(UserIdentity userIdentity) {
        log.info("{}::generate -> Generating MFA Auth Response for user: {}", getClass().getSimpleName(), userIdentity.getUserId());
        MfaEntity mfaEntity = build(userIdentity);
        MfaEntity mfaEntityResponse = mfaEntityRepository.save(mfaEntity);
        log.info("{}::generate -> Saved MFA Entity: {}", getClass().getSimpleName(), mfaEntityResponse.toJson());
        return buildAuthResponse(mfaEntityResponse);
    }

    private AuthResponse buildAuthResponse(MfaEntity mfaEntityResponse) {
        log.info("{}::buildAuthResponse -> Building Auth Response for MFA Entity: {}", getClass().getSimpleName(), mfaEntityResponse.toJson());
        Instant now = Instant.now();
        AuthenticationProperties.TokenInfo tokenInfo = authenticationProperties.getInfo();
        long expiresAt = mfaEntityResponse.getExpiresIn().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        ResponseCookie clearCookie = ResponseCookie
                .from(tokenInfo.getRefreshTokenCookieName(), null)
                .httpOnly(true)
                .maxAge(0L)
                .path("/")
                .sameSite(tokenInfo.getIsProduction() ? "Strict" : "Lax")
                .secure(tokenInfo.getIsProduction())
                .domain(null)
                .build();
        AuthResponse authResponse = AuthResponse
                .builder()
                .tokenType(ServiceConstants.MFA_TOKEN_TYPE)
                .mfaToken(mfaEntityResponse.getToken())
                .expiresIn(expiresAt - now.toEpochMilli())
                .expiresOn(expiresAt)
                .userId(mfaEntityResponse.getUserId())
                .refreshTokenCookie(clearCookie)
                .build();
        log.info("{}::buildAuthResponse -> Built Auth Response: {}", getClass().getSimpleName(), authResponse.toJson());
        return authResponse;
    }

    private MfaEntity build(UserIdentity userIdentity) {
        log.info("{}::build -> Building MFA Entity for userId: {}", getClass().getSimpleName(), userIdentity.getUserId());
        String mfaToken = MfaTokenGenerator.get();
        int expiryMinutes = authenticationProperties.getMfa().getExpiryMinutes();
        MfaEntity mfaEntity = MfaEntity
                .builder()
                .token(mfaToken)
                .userId(userIdentity.getUserId())
                .expiresIn(LocalDateTime.now().plusMinutes(expiryMinutes))
                .remoteAddress(ipUtils.getIpv4())
                .os(servletUtil.os())
                .browser(servletUtil.browser())
                .build();
        log.info("{}::build -> Built MFA Entity : {}", getClass().getSimpleName(), mfaEntity.toJson());
        return mfaEntity;
    }
}
