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
  File: AuthResponseGenerator
 */

import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.authentication.JwtTokenGenerator;
import com.revquix.backend.auth.dao.repository.RefreshTokenRepository;
import com.revquix.backend.auth.model.RefreshToken;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.payload.response.AccessTokenResponse;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.payload.response.RefreshTokenResponse;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthResponseGenerator {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtDecoder jwtDecoder;
    private final IpUtils ipUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationProperties authenticationProperties;

    @SneakyThrows
    public AuthResponse generate(UserIdentity userIdentity) {
        log.info("{}::generate -> Generating AuthResponse for userId: {}", AuthResponseGenerator.class.getSimpleName(), userIdentity.getUserId());
        String jti = UUID.randomUUID().toString();
        AccessTokenResponse accessTokenResponse = jwtTokenGenerator.accessToken(jti, userIdentity);
        RefreshTokenResponse refreshTokenResponse = jwtTokenGenerator.refreshToken(jti, userIdentity);
        RefreshToken refreshToken = build(userIdentity, jti, refreshTokenResponse);
        RefreshToken save = refreshTokenRepository.save(refreshToken);
        log.info("{}::generate -> Saved RefreshToken with ID: {} for userId: {}", AuthResponseGenerator.class.getSimpleName(), save.getJti(), userIdentity.getUserId());
        ResponseCookie responseCookie = getAuthResponseCookie(refreshTokenResponse.getRefreshToken());
        return AuthResponse
                .builder()
                .accessToken(accessTokenResponse.getAccessToken())
                .expiresIn(accessTokenResponse.getExpiresIn())
                .userId(userIdentity.getUserId())
                .expiresOn(accessTokenResponse.getExpiresOn())
                .refreshTokenCookie(responseCookie)
                .build();
    }

    private RefreshToken build(UserIdentity userIdentity, String jti, RefreshTokenResponse refreshTokenResponse) {
        log.info("{}::build -> Building RefreshToken entity for userId: {}", AuthResponseGenerator.class.getSimpleName(), userIdentity.getUserId());
        return RefreshToken
                .builder()
                .jti(jti)
                .userId(userIdentity.getUserId())
                .expiresIn(refreshTokenResponse.getExpiresIn())
                .remoteAddress(ipUtils.getIpv4())
                .build();
    }

    private ResponseCookie getAuthResponseCookie(String refreshToken) {
        log.info("{}::getAuthResponseCookie -> Creating HttpOnly cookie for refresh token", AuthResponseGenerator.class.getSimpleName());
        AuthenticationProperties.TokenInfo tokenInfo = authenticationProperties.getInfo();
        return ResponseCookie
                .from(tokenInfo.getRefreshTokenCookieName(), refreshToken)
                .httpOnly(true)
                .maxAge(tokenInfo.getRefreshTokenExpiryDays() * 86400L)
                .path("/")
                .sameSite(tokenInfo.getIsProduction() ? "Strict" : "Lax")
                .secure(tokenInfo.getIsProduction())
                .domain(null)
                .build();
    }

}
