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

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.auth.dao.repository.OtpEntityRepository;
import com.revquix.backend.auth.enums.OtpFor;
import com.revquix.backend.auth.enums.OtpStatus;
import com.revquix.backend.auth.model.OtpEntity;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import com.revquix.backend.auth.util.OtpGenerator;
import com.revquix.backend.notification.payload.ForgotPasswordOtpPayload;
import com.revquix.backend.notification.processor.SendForgotPasswordOtpMail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordOtpProcessor {
    private final OtpEntityRepository otpEntityRepository;
    private final AuthenticationProperties authenticationProperties;
    private final PasswordEncoder passwordEncoder;
    private final SendForgotPasswordOtpMail sendForgotPasswordOtpMail;

    public void process(UserAuth userAuth) {
        log.info("{}::process -> Processing Forgot Password OTP: {}", this.getClass().getSimpleName(), userAuth.getEmail());
        Optional<OtpEntity> otpEntityOptional = otpEntityRepository.findByEmailAndOtpForAndOtpStatus(
                userAuth.getEmail(),
                OtpFor.FORGOT_PASSWORD,
                OtpStatus.ACTIVE
        );
        if (otpEntityOptional.isPresent()) {
            log.info("{}::process -> Active OTP already exists for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
            LocalDateTime now = LocalDateTime.now();
            OtpEntity otpEntity = otpEntityOptional.get();
            if (otpEntity.getExpiryDate().isAfter(now)) {
                log.info("{}::process -> Reusing existing OTP for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
                otpEntity.setUserId(userAuth.getUserId());
                otpEntityRepository.save(otpEntity);
                log.info("{}::process -> OtpEntity saved successfully for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
                return;
            } else {
                log.info("{}::process -> Existing OTP expired for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
                otpEntity.setOtpStatus(OtpStatus.EXPIRED);
                otpEntityRepository.save(otpEntity);
                log.info("{}::process -> Expired OtpEntity updated successfully for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
            }
        }
        log.info("{}::process -> Generating new OTP for email: {}", this.getClass().getSimpleName(), userAuth.getEmail());
        AuthenticationProperties.PasswordReset PasswordReset = authenticationProperties.getOtpInfo().getPasswordReset();
        String otp = OtpGenerator.generate(PasswordReset.getOtpSize());
        OtpEntity otpEntity = buildOtpEntity(userAuth, otp);
        OtpEntity otpEntityResponse = otpEntityRepository.save(otpEntity);
        log.info("{}::process -> New OtpEntity saved successfully for email: {}, otpEntity: {}", this.getClass().getSimpleName(), userAuth.getEmail(), otpEntityResponse.toJson());
        if (PasswordReset.isMailEnabled()) {
            log.info("{}::process -> Forgot Password OTP mail sending is enabled, OTP: {}", this.getClass().getSimpleName(), otpEntity.getOtp());
            sendForgotPasswordOtpMail.execute(new ForgotPasswordOtpPayload(otp, otpEntity.getEmail()));
        }
        if (PasswordReset.isLogEnabled()) {
            log.info("{}::process -> Forgot Password OTP for email: {}, otp: {}", this.getClass().getSimpleName(), userAuth.getEmail(), otp);
        }
    }

    private OtpEntity buildOtpEntity(UserAuth userAuth, String otp) {
        AuthenticationProperties.PasswordReset PasswordReset = authenticationProperties.getOtpInfo().getPasswordReset();
        OtpEntity otpEntity = OtpEntity
                .builder()
                .otp(passwordEncoder.encode(otp))
                .userId(userAuth.getUserId())
                .email(userAuth.getEmail())
                .otpFor(OtpFor.FORGOT_PASSWORD)
                .expiryDate(LocalDateTime.now().plusMinutes(PasswordReset.getOtpExpiryMinutes()))
                .timesSent(0)
                .otpStatus(OtpStatus.ACTIVE)
                .build();
        log.info("{}::buildOtpEntity -> Built new OtpEntity for email: {}, otpEntity:{}", getClass().getSimpleName(), userAuth.getEmail(), otpEntity.toJson());
        return otpEntity;
    }
}
