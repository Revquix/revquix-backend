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
package com.revquix.backend.auth.guardrails;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: GenericUserValidator
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.auth.model.UserAuth;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class GenericUserValidator {

    public static void validate(UserAuth userAuth) {
        log.info("{}::validate Validating UserAuth for userId: {}", GenericUserValidator.class.getSimpleName(), userAuth.getUserId());
        if (!userAuth.isEnabled()) {
            throw new AuthenticationException(ErrorData.USER_NOT_ENABLED);
        }
        if (!userAuth.getIsAccountNonLocked()) {
            throw new AuthenticationException(ErrorData.ACCOUNT_LOCKED);
        }
        if (!userAuth.isEmailVerified()) {
            throw new AuthenticationException(ErrorData.EMAIL_NOT_VERIFIED);
        }
    }

    public static void validateThird(UserAuth userAuth) {
        log.info("{}::validateThird Validating UserAuth for userId: {}", GenericUserValidator.class.getSimpleName(), userAuth.getUserId());
        if (!userAuth.isEnabled()) {
            throw new AuthenticationException(ErrorData.USER_NOT_ENABLED_THIRD);
        }
        if (!userAuth.getIsAccountNonLocked()) {
            throw new AuthenticationException(ErrorData.ACCOUNT_LOCKED_THIRD);
        }
        if (!userAuth.isEmailVerified()) {
            throw new AuthenticationException(ErrorData.EMAIL_NOT_VERIFIED_THIRD);
        }
    }
}
