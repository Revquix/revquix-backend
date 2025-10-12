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
  File: RefreshTokenAuthenticationHelper
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.cache.RefreshTokenCache;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.RefreshTokenRepository;
import com.revquix.backend.auth.guardrails.GenericUserValidator;
import com.revquix.backend.auth.model.RefreshToken;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenAuthentication {

    private final JwtDecoder jwtDecoder;
    private final JwtDataProvider jwtDataProvider;
    private final RefreshTokenCache refreshTokenCache;
    private final AuthenticationProperties authenticationProperties;
    private final IpUtils ipUtils;
    private final UserAuthCache userAuthCache;
    private final RefreshTokenRepository refreshTokenRepository;

    public Authentication authenticate(String token) {
        log.info("{}::authenticate -> Authenticating refresh token", this.getClass().getSimpleName());
        Jwt jwt = validateToken(token);
        String jti = jwt.getId();
        RefreshToken refreshToken = refreshTokenCache.findById(jti)
                .orElseThrow(() -> new AuthenticationException(ErrorData.JTI_NOT_PRESENT_DB));
        String userId = jwt.getSubject();
        if (!userId.equals(refreshToken.getUserId())) {
            throw new AuthenticationException(ErrorData.JTI_USER_NOT_MATCHED_WITH_TOKEN);
        }
        validateRemoteAddress(jwt);
        UserAuth userAuth = userAuthCache.findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorData.USER_NOT_FOUND_FOR_GIVEN_TOKEN));
        GenericUserValidator.validate(userAuth);
        UserIdentity userIdentity = UserIdentity.create(userAuth);
        refreshTokenCache.deleteById(jti);
        refreshTokenRepository.delete(refreshToken);
        log.info("{}::authenticate -> Refresh Token delete from cache and database for jti: {}", this.getClass().getSimpleName(), jti);
        return new RevquixAuthenticationToken(userIdentity, null, userIdentity.getAuthorities());
    }

    private Jwt validateToken(String token) {
        log.info("{}::validateToken -> Validate token endpoint called", this.getClass().getSimpleName());
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (isExpired(jwt)) {
                throw new AuthenticationException(ErrorData.REFRESH_TOKEN_EXPIRED);
            }
            return jwt;
        } catch (JwtValidationException exception) {
            Collection<OAuth2Error> errors = exception.getErrors();
            boolean isExpired = false;
            for (OAuth2Error error : errors) {
                if (error.getDescription().contains("expired")) {
                    isExpired = true;
                    break;
                }
            }
            if (isExpired) throw new AuthenticationException(ErrorData.REFRESH_TOKEN_EXPIRED);
            else throw new AuthenticationException(ErrorData.MALFORMED_REFRESH_TOKEN);
        } catch (BadJwtException exception) {
            throw new AuthenticationException(ErrorData.MALFORMED_REFRESH_TOKEN);
        }
    }

    private boolean isExpired(Jwt jwt) {
        log.info("{}::isExpired -> Validating token expiration", getClass().getSimpleName());
        Instant expiration = jwt.getExpiresAt();
        if (expiration != null && expiration.isBefore(Instant.now())) {
            log.error("{}::handleExpiration -> Token is expired", getClass().getSimpleName());
            return true;
        }
        return false;
    }

    private void validateRemoteAddress(Jwt jwt) {
        log.debug("{}::validateRemoteAddress -> Validating remote address from JWT", this.getClass().getSimpleName());
        String tokenRemoteAddress = jwtDataProvider.extractRemoteAddress(jwt);
        if (authenticationProperties.getInfo().getIsRemoteAddressAuthentication()) {
            log.debug("{}::validateRemoteAddress -> Remote address validation is enabled", this.getClass().getSimpleName());
            if (!tokenRemoteAddress.equals(ipUtils.getIpv4())) {
                log.error("{}::validateRemoteAddress -> Remote address does not match. Token remote address: {}, Current remote address: {}", this.getClass().getSimpleName(), tokenRemoteAddress, ipUtils.getIpv4());
                throw new AuthenticationException(ErrorData.INVALID_REMOTE_ADDRESS_REFRESH_TOKEN);
            }
        }
    }
}
