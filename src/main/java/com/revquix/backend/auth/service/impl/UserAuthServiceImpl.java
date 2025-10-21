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
package com.revquix.backend.auth.service.impl;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: UserAuthServiceImpl
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.request.MfaRequest;
import com.revquix.backend.auth.payload.response.ModuleResponse;
import com.revquix.backend.auth.service.UserAuthService;
import com.revquix.backend.auth.util.IdentityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    private final UserAuthCache userAuthCache;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<ModuleResponse> toggleMfa(MfaRequest mfaRequest) {
        log.info("{}::enableMfa -> Enable MFA service called", getClass().getSimpleName());
        String userId = IdentityProvider.getOrThrow().getUserId();
        UserAuth userAuth = userAuthCache.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorData.USER_NOT_FOUND_BY_ID));
        if (!passwordEncoder.matches(mfaRequest.getPassword(), userAuth.getPassword())) {
            throw new BadRequestException(ErrorData.INCORRECT_PASSWORD);
        }
        userAuth.setMfaEnabled(!userAuth.isMfaEnabled());
        UserAuth userAuthResponse = userAuthRepository.save(userAuth);
        boolean mfaEnabled = userAuthResponse.isMfaEnabled();
        log.info("{}::enableMfa -> MFA {} successfully for userId: {}", getClass().getSimpleName(), mfaEnabled, userId);
        userAuthCache.put(userAuthResponse);
        return ResponseEntity.ok(
                ModuleResponse
                        .builder()
                        .message(String.format("MFA %s successfully", mfaEnabled ? "enabled" : "disabled"))
                        .build()
        );
    }
}
