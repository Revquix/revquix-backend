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
  File: EmailValidator
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.notification.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailValidator {

    private final MailProperties mailProperties;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public void validate(String email) throws BadRequestException {
        log.info("{}::validate -> Validating email: {}", EmailValidator.class.getSimpleName(), email);
        if (email == null) {
            throw new BadRequestException(ErrorData.EMAIL_MANDATORY);
        }
        boolean matches = EMAIL_PATTERN.matcher(email).matches();
        if (!matches) {
            throw new BadRequestException(ErrorData.EMAIL_NOT_VALID);
        }
        int atIndex = email.lastIndexOf("@");
        String domain = email.substring(atIndex + 1);
        MailProperties.MailDomain mailDomain = mailProperties.getMailDomain();
        if (!mailDomain.isEnabled()) {
            log.info("{}::validate -> Email domain validation is disabled. Skipping validation for email: {}", getClass().getSimpleName(), email);
            return;
        }
        if (!mailDomain.getAllowedDomains().contains(domain)) {
            throw new BadRequestException(
                    ErrorData.INVALID_MAIL_DOMAIN,
                    String.format("Email domain '%s' is not allowed.", domain));
        }
    }
}
