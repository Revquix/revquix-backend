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
import com.revquix.backend.auth.authentication.RefreshTokenAuthentication;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.OtpEntityRepository;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.enums.OtpFor;
import com.revquix.backend.auth.enums.OtpStatus;
import com.revquix.backend.auth.events.RegisterUserOtpEvent;
import com.revquix.backend.auth.guardrails.*;
import com.revquix.backend.auth.model.OtpEntity;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.payload.response.LogoutResponse;
import com.revquix.backend.auth.payload.response.ModuleResponse;
import com.revquix.backend.auth.processor.AuthResponseGenerator;
import com.revquix.backend.auth.processor.LogoutProcessor;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import com.revquix.backend.auth.service.AuthService;
import com.revquix.backend.auth.transformer.RegisterUserTransformer;
import com.revquix.backend.auth.util.RefreshTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAuthRepository userAuthRepository;
    private final RegisterUserTransformer registerUserTransformer;
    private final UserAuthCache userAuthCache;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OtpEntityRepository otpEntityRepository;
    private final AuthenticationManager authenticationManager;
    private final InstanceValidator instanceValidator;
    private final AuthResponseGenerator authResponseGenerator;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenAuthentication refreshTokenAuthentication;
    private final PasswordEncoder passwordEncoder;
    private final LogoutProcessor logoutProcessor;
    private final AuthenticationProperties authenticationProperties;

    @Override
    @Transactional
    public ResponseEntity<AuthResponse> token(String entrypoint, String password) {
        log.info("{}::validate -> Validating token: {}", UsernameValidator.class.getSimpleName(), entrypoint);
        entrypoint = entrypoint.toLowerCase();
        EntrypointValidator.validate(entrypoint);
        PasswordValidator.validate(password);
        Authentication userAuthentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(entrypoint, password));
        UserIdentity userIdentity = (UserIdentity) userAuthentication.getPrincipal();
        instanceValidator.validate(userIdentity);
        AuthResponse authResponse = authResponseGenerator.generate(userIdentity);
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);
        return ResponseEntity
                .accepted()
                .header(HttpHeaders.SET_COOKIE, authResponse.getRefreshTokenCookie().toString())
                .body(authResponse);
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

    @Override
    @Transactional
    public ResponseEntity<ModuleResponse> registerOtpVerification(String userId, String otp) {
        log.info("{}::registerOtpVerification -> Verifying OTP for userId: {}", this.getClass().getSimpleName(), userId);
        OtpEntity otpEntity = otpEntityRepository.findByUserIdAndOtpForAndOtpStatus(
                userId,
                OtpFor.REGISTER,
                OtpStatus.ACTIVE
        ).orElseThrow(() -> new BadRequestException(ErrorData.NOT_OTP_FOR_REGISTER_FOUND));
        if (!passwordEncoder.matches(otp, otpEntity.getOtp())) {
            throw new BadRequestException(ErrorData.INVALID_REGISTER_OTP);
        }
        LocalDateTime now = LocalDateTime.now();
        Optional<UserAuth> userAuthOptional = userAuthCache.findById(userId);
        if (userAuthOptional.isEmpty()) {
            otpEntity.setOtpStatus(OtpStatus.DELETED);
            OtpEntity otpEntityResponse = otpEntityRepository.save(otpEntity);
            log.info("{}::registerOtpVerification -> OTP marked as DELETED for userId: {}, response: {}", this.getClass().getSimpleName(), userId, otpEntityResponse.toJson());
            throw new BadRequestException(ErrorData.USER_NOT_FOUND_BY_ID);
        }
        UserAuth userAuth = userAuthOptional.get();
        if (Boolean.TRUE.equals(userAuth.isEmailVerified())) {
            otpEntity.setOtpStatus(OtpStatus.DELETED);
            OtpEntity otpEntityResponse = otpEntityRepository.save(otpEntity);
            log.info("{}::registerOtpVerification -> OTP marked as DELETED for userId: {}, response: {}", this.getClass().getSimpleName(), userId, otpEntityResponse.toJson());
            throw new BadRequestException(ErrorData.USER_ALREADY_REGISTERED);
        }
        if (otpEntity.getExpiryDate().isBefore(now)) {
            throw new BadRequestException(ErrorData.REGISTRATION_OTP_EXPIRED);
        }
        userAuth.setEmailVerified(true);
        UserAuth userAuthResponse = userAuthRepository.save(userAuth);
        log.info("{}::registerOtpVerification -> User email verified successfully for userId: {}, response: {}", this.getClass().getSimpleName(), userId, userAuthResponse.toJson());
        userAuthCache.put(userAuthResponse);
        otpEntity.setOtpStatus(OtpStatus.DELETED);
        OtpEntity otpEntityResponse = otpEntityRepository.save(otpEntity);
        log.info("{}::registerOtpVerification -> OTP marked as DELETED for userId: {}, response: {}", this.getClass().getSimpleName(), userId, otpEntityResponse.toJson());
        return ResponseEntity.ok(
                ModuleResponse
                        .builder()
                        .message("User email verified successfully. You can now log in to your account.")
                        .userId(userAuth.getUserId())
                        .build()
        );
    }

    @Override
    @Transactional
    public ResponseEntity<AuthResponse> refreshToken() {
        log.info("{}::refreshToken -> Refresh token endpoint called", this.getClass().getSimpleName());
        String refreshToken = refreshTokenProvider.get();
        Authentication authentication = refreshTokenAuthentication.authenticate(refreshToken);
        UserIdentity userIdentity = (UserIdentity) authentication.getPrincipal();
        instanceValidator.validate(userIdentity);
        AuthResponse authResponse = authResponseGenerator.generate(userIdentity);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity
                .accepted()
                .header(HttpHeaders.SET_COOKIE, authResponse.getRefreshTokenCookie().toString())
                .body(authResponse);
    }

    @Override
    public ResponseEntity<Object> logout() {
        log.info("{}::logout", this.getClass().getSimpleName());
        String message = logoutProcessor.process();
        AuthenticationProperties.TokenInfo tokenInfo = authenticationProperties.getInfo();
        ResponseCookie clearCookie = ResponseCookie
                .from(tokenInfo.getRefreshTokenCookieName(), null)
                .httpOnly(true)
                .maxAge(0L)
                .path("/")
                .sameSite(tokenInfo.getIsProduction() ? "Strict" : "Lax")
                .secure(tokenInfo.getIsProduction())
                .domain(null)
                .build();
        return ResponseEntity
                .accepted()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(LogoutResponse.builder().localizedMessage(message).build());
    }
}
