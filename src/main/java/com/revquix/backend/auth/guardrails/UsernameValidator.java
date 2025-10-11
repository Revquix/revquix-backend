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
  File: UsernameValidator
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class UsernameValidator {

    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 16;

    public static void validate(String username) {
        log.info("{}::validate -> Validating username: {}", UsernameValidator.class.getSimpleName(), username);
        if (username == null || username.isBlank()) {
            throw new AuthenticationException(ErrorData.USERNAME_MANDATORY);
        }

        if (username.length() < MIN_LENGTH) {
            throw new AuthenticationException(ErrorData.USERNAME_TOO_SHORT);
        }

        if (username.length() > MAX_LENGTH) {
            throw new AuthenticationException(ErrorData.USERNAME_TOO_LONG);
        }

        if (username.contains("@")) {
            throw new AuthenticationException(ErrorData.USERNAME_INVALID_CHARACTER);
        }

        if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
            throw new AuthenticationException(ErrorData.USERNAME_INVALID_START);
        }
    }
}
