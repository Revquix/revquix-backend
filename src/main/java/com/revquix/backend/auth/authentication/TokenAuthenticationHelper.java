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
  File: TokenAuthenticationHelper
 */

import com.revquix.backend.application.enums.Status;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.application.utils.OutputStreamExceptionGenerator;
import com.revquix.backend.auth.cache.RefreshTokenCache;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.model.RefreshToken;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import com.revquix.backend.auth.util.TokenExtractorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationHelper {

    private final JwtDecoder jwtDecoder;
    private final JwtDataProvider jwtDataProvider;
    private final UserAuthCache userAuthCache;
    private final AuthenticationProperties authenticationProperties;
    private final IpUtils ipUtils;
    private final RefreshTokenCache refreshTokenCache;

    public UserIdentity authenticate(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        log.debug("TokenAuthenticationHelper::authenticate -> Authenticating token from request");
        Jwt jwt = jwtDecoder.decode(TokenExtractorUtil.extractToken(request));
        UserAuth userAuth = getUser(jwt, httpServletResponse);
        boolean validUser = isValidUser(userAuth, httpServletResponse);
        if (!validUser) {
            log.error("{}::authenticate -> User is not valid", this.getClass().getSimpleName());
            return null;
        }
        boolean isValidRemoteAddress = isValidRemoteAddress(jwt, httpServletResponse);
        if (!isValidRemoteAddress) return null;
        boolean isValidJti = isValidJti(jwt, userAuth, httpServletResponse);
        if (!isValidJti) return null;
        log.info("{}::authenticate -> Successfully authenticated user with userId: {}", this.getClass().getSimpleName(), userAuth.getUserId());
        return UserIdentity.create(userAuth);
    }

    private boolean isValidJti(Jwt jwt, UserAuth userAuth, HttpServletResponse httpServletResponse) {
        log.debug("{}::isValidJti -> Validating JTI from JWT", this.getClass().getSimpleName());
        String tokenJti = jwt.getId();
        Optional<RefreshToken> refreshTokenOptional = refreshTokenCache.findById(tokenJti);
        if (refreshTokenOptional.isEmpty()) {
            log.error("{}::isValidJti -> Refresh token not found in cache or database for JTI: {}", this.getClass().getSimpleName(), tokenJti);
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.INVALID_TOKEN_JTI, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return false;
        }
        RefreshToken refreshToken = refreshTokenOptional.get();
        if (!refreshToken.getUserId().equals(userAuth.getUserId())) {
            log.error("{}::isValidJti -> Token JTI does not match user ID. Token JTI: {}, User ID: {}", this.getClass().getSimpleName(), tokenJti, userAuth.getUserId());
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.INVALID_TOKEN_JTI, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return false;
        }
        return true;
    }

    private boolean isValidRemoteAddress(Jwt jwt, HttpServletResponse httpServletResponse) {
        log.debug("{}::isValidRemoteAddress -> Validating remote address from JWT", this.getClass().getSimpleName());
        String tokenRemoteAddress = jwtDataProvider.extractRemoteAddress(jwt);
        if (authenticationProperties.getInfo().getIsRemoteAddressAuthentication()) {
            log.debug("{}::isValidRemoteAddress -> Remote address validation is enabled", this.getClass().getSimpleName());
            if (!tokenRemoteAddress.equals(ipUtils.getIpv4())) {
                log.error("{}::isValidRemoteAddress -> Remote address does not match. Token remote address: {}, Current remote address: {}", this.getClass().getSimpleName(), tokenRemoteAddress, ipUtils.getIpv4());
                OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.INVALID_REMOTE_ADDRESS, HttpStatus.UNAUTHORIZED, httpServletResponse);
                return false;
            }
        }
        return true;
    }

    private boolean isValidUser(UserAuth userAuth, HttpServletResponse httpServletResponse) {
        log.debug("{}::validateUser -> Validating user status for userId: {}", this.getClass().getSimpleName(), userAuth.getUserId());
        if (!userAuth.isEnabled()) {
            log.error("{}::validateUser -> User is disabled with userId: {}", this.getClass().getSimpleName(), userAuth.getUserId());
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.USER_NOT_ENABLED, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return false;
        }
        if (!userAuth.getIsAccountNonLocked()) {
            log.error("{}::validateUser -> User account is locked with userId: {}", this.getClass().getSimpleName(), userAuth.getUserId());
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.ACCOUNT_LOCKED, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return false;
        }
        if (!userAuth.getStatus().equals(Status.ACTIVE)) {
            log.error("{}::validateUser -> User status is not active with userId: {}", this.getClass().getSimpleName(), userAuth.getUserId());
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.USER_NOT_ACTIVE, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return false;
        }
        return true;
    }

    private UserAuth getUser(Jwt jwt, HttpServletResponse httpServletResponse) {
        log.info("{}::validateUser -> Validating user from JWT", this.getClass().getSimpleName());
        String userId = jwtDataProvider.extractUserId(jwt);
        Optional<UserAuth> userAuthOptional = userAuthCache.findById(userId);
        if (userAuthOptional.isEmpty()) {
            log.error("{}::getUser -> User not found with id: {}", userId);
            OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.USER_NOT_FOUND_FOR_GIVEN_TOKEN, HttpStatus.UNAUTHORIZED, httpServletResponse);
            return null;
        }
        return userAuthOptional.get();
    }
}
