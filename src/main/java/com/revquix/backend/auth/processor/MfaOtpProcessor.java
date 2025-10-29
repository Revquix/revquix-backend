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
  File: MfaOtpProcessor
 */

import com.revquix.backend.auth.model.MfaEntity;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import com.revquix.backend.notification.payload.MfaOtpPayload;
import com.revquix.backend.notification.processor.SendMfaOtpMail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaOtpProcessor {

    private final AuthenticationProperties authenticationProperties;
    private final SendMfaOtpMail sendMfaOtpMail;

    public void process(String email, String otp) {
        log.info("{}::process -> Processing MFA OTP for email: {}", getClass().getSimpleName(), email);
        AuthenticationProperties.Mfa mfa = authenticationProperties.getMfa();
        if (mfa.isEnabled()) {
            log.info("{}::process -> MFA is enabled in properties. Proceeding with OTP processing for MFA Entity: {}", getClass().getSimpleName());
            sendMfaOtpMail.execute(MfaOtpPayload.builder().otp(otp).email(email).build());
        } else {
            log.warn("{}::process -> MFA is disabled in properties. Skipping OTP processing for MFA Entity: {}", getClass().getSimpleName());
        }
    }
}
