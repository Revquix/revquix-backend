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
package com.revquix.backend.auth.authentication;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: JwtTokenGenerator
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.payload.response.AccessTokenResponse;
import com.revquix.backend.auth.payload.response.RefreshTokenResponse;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final AuthenticationProperties authenticationProperties;
    private final IpUtils ipUtils;

    public AccessTokenResponse accessToken(String jti, UserIdentity userIdentity) {
        log.info("{}::accessToken -> Generating access token for userId: {}", JwtTokenGenerator.class.getSimpleName(), userIdentity.getUserId());
        Instant now = Instant.now();
        AuthenticationProperties.TokenInfo tokenInfo = authenticationProperties.getInfo();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet
                .builder()
                .issuedAt(now)
                .notBefore(now)
                .subject(userIdentity.getUserId())
                .issuer(ServiceConstants.REVQUIX_ISSUER)
                .expiresAt(now.plus(tokenInfo.getAccessTokenExpiryMinutes(), ChronoUnit.MINUTES))
                .claim(ServiceConstants.REMOTE_ADDRESS, ipUtils.getIpv4())
                .id(jti)
                .build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        Instant expiresAt = jwtClaimsSet.getExpiresAt();
        return AccessTokenResponse
                .builder()
                .accessToken(tokenValue)
                .expiresOn(expiresAt.toEpochMilli())
                .expiresIn(expiresAt.toEpochMilli() - now.toEpochMilli())
                .build();
    }

    public RefreshTokenResponse refreshToken(String jti, UserIdentity userIdentity) {
        log.info("{}::refreshToken -> Generating refresh token for userId: {}", JwtTokenGenerator.class.getSimpleName(), userIdentity.getUserId());
        Instant now = Instant.now();
        AuthenticationProperties.TokenInfo tokenInfo = authenticationProperties.getInfo();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet
                .builder()
                .issuedAt(now)
                .notBefore(now)
                .subject(userIdentity.getUserId())
                .id(jti)
                .expiresAt(now.plus(tokenInfo.getRefreshTokenExpiryDays(), ChronoUnit.DAYS))
                .claim(ServiceConstants.REMOTE_ADDRESS, ipUtils.getIpv4())
                .issuer(ServiceConstants.REVQUIX_ISSUER)
                .build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        return RefreshTokenResponse
                .builder()
                .refreshToken(tokenValue)
                .expiresIn(jwtClaimsSet.getExpiresAt().toEpochMilli())
                .build();
    }
}
