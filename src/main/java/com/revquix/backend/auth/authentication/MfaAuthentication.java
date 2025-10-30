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
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.cache.RefreshTokenCache;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.MfaEntityRepository;
import com.revquix.backend.auth.dao.repository.RefreshTokenRepository;
import com.revquix.backend.auth.guardrails.GenericUserValidator;
import com.revquix.backend.auth.model.MfaEntity;
import com.revquix.backend.auth.model.RefreshToken;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.payload.request.VerifyMfaRequest;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaAuthentication {

    private final JwtDecoder jwtDecoder;
    private final JwtDataProvider jwtDataProvider;
    private final RefreshTokenCache refreshTokenCache;
    private final AuthenticationProperties authenticationProperties;
    private final IpUtils ipUtils;
    private final UserAuthCache userAuthCache;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MfaEntityRepository mfaEntityRepository;
    private final PasswordEncoder passwordEncoder;

    public Authentication authenticate(VerifyMfaRequest verifyMfaRequest) {
        log.info("{}::authenticate -> Authenticating MFA Token", this.getClass().getSimpleName());
        MfaEntity mfaEntity = mfaEntityRepository.findByToken(verifyMfaRequest.getToken())
                .orElseThrow(() -> new AuthenticationException(ErrorData.INVALID_MFA_TOKEN));
        UserAuth userAuth = userAuthCache.findById(mfaEntity.getUserId())
                .orElseThrow(() -> new AuthenticationException(ErrorData.USER_NOT_FOUND_BY_ID));
        if (!userAuth.isMfaEnabled()) {
            log.info("{}::verifyMfa -> MFA not enabled for userId: {}, deleting MFA entity", this.getClass().getSimpleName(), mfaEntity.getUserId());
            mfaEntityRepository.delete(mfaEntity);
            log.info("{}::verifyMfa -> Deleted MFA entity for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
            throw new AuthenticationException(ErrorData.MFA_NOT_ENABLED);
        }
        LocalDateTime now = LocalDateTime.now();
        if (mfaEntity.getExpiresIn().isBefore(now)) {
            log.info("{}::verifyMfa -> MFA OTP expired for userId: {}, deleting MFA entity", this.getClass().getSimpleName(), mfaEntity.getUserId());
            mfaEntityRepository.delete(mfaEntity);
            log.info("{}::verifyMfa -> Deleted MFA entity for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
            throw new BadRequestException(ErrorData.MFA_EXPIRED);
        }
        if (!mfaEntity.getRemoteAddress().equals(ipUtils.getIpv4())) {
            log.info("{}::verifyMfa -> Remote address mismatch for userId: {}, deleting MFA entity", this.getClass().getSimpleName(), mfaEntity.getUserId());
            mfaEntityRepository.delete(mfaEntity);
            log.info("{}::verifyMfa -> Deleted MFA entity for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
            throw new AuthenticationException(ErrorData.INVALID_REMOTE_ADDRESS_MFA);
        }
        if (!passwordEncoder.matches(verifyMfaRequest.getOtp(), mfaEntity.getOtp())) {
            throw new AuthenticationException(ErrorData.INVALID_MFA_OTP);
        }
        GenericUserValidator.validate(userAuth);
        UserIdentity userIdentity = UserIdentity.create(userAuth);
        log.info("{}::authenticate -> MFA Authentication successful, deleting MFA entity for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
        mfaEntityRepository.delete(mfaEntity);
        log.info("{}::authenticate -> Deleted MFA entity for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
        log.info("{}::authenticate -> MFA Authentication successful for userId: {}", this.getClass().getSimpleName(), mfaEntity.getUserId());
        return new RevquixAuthenticationToken(userIdentity, null, userIdentity.getAuthorities());
    }
}
