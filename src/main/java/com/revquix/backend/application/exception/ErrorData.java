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
package com.revquix.backend.application.exception;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: ErrorData
 */

import lombok.Getter;

@Getter
public enum ErrorData {

    INTERNAL_SERVER_ERROR("IE-1", "Something went wrong at server side, please try again later or contact support team."),
    ACCESS_DENIED_ERROR_CODE("DE-2", "You don't have permission to access this resource."),
    FAILED_TO_GENERATE_SEQUENCE("IE-3", "Failed to generate sequence"),
    SEQUENCE_NULL_OR_EMPTY("IE-4", "Sequence name is null or empty"),
    ID_GENERATION_FAILED("IE-5", "ID generation failed"),
    NO_USER_WITH_USERNAME("DE-6", "No user found with the given username"),
    NO_USER_WITH_EMAIL("DE-7", "No user found with the given email"),
    INCORRECT_PASSWORD("DE-8", "The password you have entered is incorrect"),
    USER_NOT_ENABLED("DE-9", "User is not enabled, please contact support team"),
    ACCOUNT_LOCKED("DE-10", "User account is locked, please contact support team"),
    EMAIL_NOT_VERIFIED("DE-11", "Email is not verified, please verify your email first"),
    EXCEPTION_WHILE_FETCHING_PRIVATE_KEY("IE-12", "Exception occurred while fetching private key for JWT"),
    EXCEPTION_WHILE_FETCHING_PUBLIC_KEY("IE-14", "Exception occurred while fetching public key for JWT"),;

    ;
    private static final String ERROR_PREFIX = "RQ-ERR-";

    private final String code;
    private final String message;

    ErrorData(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
