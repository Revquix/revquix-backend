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
  File: AuthServiceImpl
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.application.utils.MdcUtils;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.events.RegisterUserOtpEvent;
import com.revquix.backend.auth.guardrails.EmailValidator;
import com.revquix.backend.auth.guardrails.PasswordValidator;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.payload.response.ModuleResponse;
import com.revquix.backend.auth.service.AuthService;
import com.revquix.backend.auth.transformer.RegisterUserTransformer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAuthRepository userAuthRepository;
    private final RegisterUserTransformer registerUserTransformer;
    private final UserAuthCache userAuthCache;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ResponseEntity<AuthResponse> token(String entrypoint, String password) {
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<ModuleResponse> registerUser(String email, String password) {
        log.info("{}::registerUser -> Registering user with email: {}", this.getClass().getSimpleName(), email);
        email = email.toLowerCase();
        EmailValidator.validate(email);
        PasswordValidator.validate(password);
        Optional<UserAuth> userAuthOptional = userAuthRepository.findByEmail(email);
        if (userAuthOptional.isPresent()) {
            log.info("{}::registerUser -> User already exists with email: {}", this.getClass().getSimpleName(), email);
            UserAuth userAuth = userAuthOptional.get();
            if (Boolean.TRUE.equals(userAuth.isEmailVerified())) {
                throw new BadRequestException(ErrorData.USER_ALREADY_REGISTERED);
            } else {
                log.warn("{}::registerUser -> Deleting User Auth Data as user not enabled and getting new request for register email: {}", this.getClass().getSimpleName(), email);
                userAuthRepository.delete(userAuth);
                userAuthCache.deleteById(userAuth.getUserId());
                log.warn("{}::registerUser -> Deleted User Auth Data for email: {}", this.getClass().getSimpleName(), email);
            }
        }
        UserAuth transformedUserAuth = registerUserTransformer.transform(email, password);
        UserAuth userAuth = userAuthRepository.save(transformedUserAuth);
        log.info("{}::registerUser -> Registered user successfully with email: {}", this.getClass().getSimpleName(), email);
        userAuthCache.put(userAuth);
        applicationEventPublisher.publishEvent(new RegisterUserOtpEvent(userAuth, MdcUtils.getBreadcrumbId()));
        return ResponseEntity.ok(
                ModuleResponse
                        .builder()
                        .message("User registered successfully. Please verify your email to activate your account.")
                        .userId(userAuth.getUserId())
                        .build()
        );
    }
}
