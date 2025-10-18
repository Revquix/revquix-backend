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
    EXCEPTION_WHILE_FETCHING_PUBLIC_KEY("IE-14", "Exception occurred while fetching public key for JWT"),
    TOKEN_EXPIRED("DE-15", "JWT token has expired, please login again"),
    MALFORMED_TOKEN("DE-16", "The JWT token is malformed, please provide a valid token"),
    USER_NOT_FOUND_FOR_GIVEN_TOKEN("DE-17", "No user found for the given token"),
    EMAIL_MANDATORY("DE-18", "Email is mandatory to continue. Please enter valid mail address"),
    EMAIL_NOT_VALID("DE-19", "Email address is not valid, please enter valid email address"),
    PASSWORD_MANDATORY("DE-20", "Password is mandatory to continue. Please enter valid password"),
    INVALID_PASSWORD("DE-21", "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character"),
    USER_ALREADY_REGISTERED("DE-22", "User is already registered with the given email address"),
    USER_ROLE_NOT_FOUND("IE-23", "User role not found in the system"),
    RATE_LIMIT_EXCEEDED("DE-24", "You have exceeded the number of allowed requests. Please try again later."),
    NOT_OTP_FOR_REGISTER_FOUND("DE-25", "No OTP found for registration with the given email"),
    INVALID_REGISTER_OTP("DE-26", "The OTP you have entered is invalid. Please try again with the OTP sent on your Email"),
    USER_NOT_FOUND_BY_ID("DE-27", "No user found with the given id"),
    REGISTRATION_OTP_EXPIRED("DE-28", "The OTP has expired. Please request a new OTP to continue with registration"),
    ENTRYPOINT_MANDATORY("DE-29", "Please enter your email or username to continue"),
    USERNAME_MANDATORY("DE-30", "Username is mandatory to continue. Please enter valid username"),
    USERNAME_TOO_SHORT("DE-31", "Username must be at least 2 characters long"),
    USERNAME_INVALID_CHARACTER("DE-32", "Username must not contain '@' character"),
    USERNAME_INVALID_START("DE-33", "Username must start with a letter and contain only alphanumeric characters and underscores"),
    USERNAME_TOO_LONG("DE-34", "Username must not exceed 16 characters"),
    MULTIPLE_ACTIVE_PROFILES_FOUND("IE-35", "Multiple active profiles found for the application, please contact support team"),
    NO_ACTIVE_PROFILE_FOUND("IE-36", "No active profile found for the application, please contact support team"),
    REQUIRED_ROLES_NOT_PRESENT("DE-37", "You don't have required roles to access this Instance"),
    USER_NOT_ACTIVE("DE-38", "User is not active or might be deleted"),
    INVALID_REMOTE_ADDRESS("DE-39", "Remote address is not valid, please login again"),
    INVALID_TOKEN_JTI("DE-40", "The token identifier (jti) is invalid, please login again"),
    REFRESH_TOKEN_MISSING("DE-41", "User is not logged in. Please login"),
    REFRESH_TOKEN_EXPIRED("DE-42", "Refresh token has expired, please login again"),
    MALFORMED_REFRESH_TOKEN("DE-43", "The refresh token is malformed, please provide a valid token"),
    JTI_NOT_PRESENT_DB("DE-44", "The token identifier (jti) is not present in database, please login again"),
    JTI_USER_NOT_MATCHED_WITH_TOKEN("DE-45", "The token identifier (jti) does not match with the user, please login again"),
    INVALID_REMOTE_ADDRESS_REFRESH_TOKEN("DE-46", "Remote address does not match with the one present in refresh token, please login again"),
    USER_NOT_LOGGED_IN("DE-47", "User is not logged in, please login to continue"),
    ASSIGNEE_USER_NOT_FOUND("DE-48", "No user found with the given userId to assign roles"),
    USER_ID_NULL("DE-49", "UserId is null or empty"),
    ROLES_EMPTY("DE-50", "Roles list is empty"),
    ROLE_LIST_CONTAINS_NULL("DE-51", "Roles list contains null value"),
    ROLES_ALREADY_ASSIGNED("DE-52", "All the roles are already assigned to the user"),
    ASSIGNER_NOT_HAVE_ROLES("DE-53", "You don't have required roles to assign to the user"),
    ROLE_NOT_FOUND_NAME("DE-54", "No role found with the given name"),
    INVALID_ROLES_PROVIDED("DE-55", "Invalid Roles provided, please provide valid roles"),
    ONLY_USER_ROLE_NOT_ALLOWED("DE-56", "Operation not allowed with only 'user' role."),
    ADMIN_ROLE_CANNOT_BE_ASSIGNED_THROUGH_THIS_API("DE-57", "Admin role cannot be assigned through this API. Please use assign admin role API"),
    USER_NOT_ENABLED_THIRD("DE-58", "User is not enabled or might be deleted. So cannot assign roles to that user"),
    ACCOUNT_LOCKED_THIRD("DE-59", "User account is locked. So cannot assign roles to that user"),
    EMAIL_NOT_VERIFIED_THIRD("DE-60", "Given user hasn't verified their email yet. So cannot assign roles to that user"),
    CANNOT_ASSIGN_ROLE_TO_SELF("DE-61", "You cannot assign roles to yourself"),
    FAILED_TO_SEND_MAIL_API_ERROR("DE-62", "Failed to send email, please try again later or contact support team"),
    USER_EMAIL_NOT_FOUND("DE-63", "User not found with the given email"),;

    private final String code;
    private final String message;

    ErrorData(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
