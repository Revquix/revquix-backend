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
  File: LogoutPrcessor
 */

import com.revquix.backend.auth.cache.RefreshTokenCache;
import com.revquix.backend.auth.dao.repository.RefreshTokenRepository;
import com.revquix.backend.auth.util.RefreshTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutProcessor {

    private final RefreshTokenProvider refreshTokenProvider;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCache refreshTokenCache;

    public String process() {
        log.info("{}::process", this.getClass().getSimpleName());
        try {
            String refreshToken = refreshTokenProvider.get();
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String jti = jwt.getId();
            refreshTokenRepository.deleteById(jti);
            refreshTokenCache.deleteById(jti);
            log.info("{}::process -> Deletion successful from Database and Cache for jti: {}", this.getClass().getSimpleName(), jti);
            return "Logged out successfully";
        } catch (JwtValidationException exception) {
            Collection<OAuth2Error> errors = exception.getErrors();
            for (OAuth2Error error : errors) {
                log.error("{}::process -> Refresh Token validation error while logout, error: {}", error.getDescription(), error);
            }
            return exception.getMessage();
        } catch (Exception exception) {
            log.error("{}::process -> Exception occurred during logout: {}", this.getClass().getSimpleName(), exception.getMessage());
            return exception.getMessage();
        }
    }
}
