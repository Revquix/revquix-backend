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
package com.revquix.backend.auth.controller;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: AuthController
 */

import com.revquix.backend.application.annotation.RateLimit;
import com.revquix.backend.application.enums.RateLimitType;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.utils.LoggedResponse;
import com.revquix.backend.auth.payload.request.ForgotPasswordRequest;
import com.revquix.backend.auth.payload.request.RegisterRequest;
import com.revquix.backend.auth.payload.request.TokenRequest;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.payload.response.ModuleResponse;
import com.revquix.backend.auth.payload.response.RegistrationResponse;
import com.revquix.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication and authorization"
)
@ApiResponses(
        value = {
                @ApiResponse(
                        description = "Invalid request parameters.",
                        responseCode = "400",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                )
        }
)
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Operation(
            summary = "Generate Authentication Token for User",
            description = "Generates a JWT authentication token for a user based on their entrypoint (email/username) and password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = TokenRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Authentication token generated successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    )
            }
    )
    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 2,
            requestsPerHour = 10,
            message = "Too many login attempts from this IP, please try again later."
    )
    ResponseEntity<AuthResponse> token(
            @Parameter(name = "entrypoint", required = true, example = "someone@example.com") @RequestParam String entrypoint,
            @Parameter(name = "password", required = true, example = "Hello@1234") @RequestParam String password
            ) {
        return LoggedResponse.call(
                ()-> authService.token(entrypoint, password),
                "Token",
                log
        );
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with the provided email and password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Register Details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "User registered successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ModuleResponse.class)
                            )
                    )
            }
    )
    @PostMapping(
            value = "/register-user",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 2,
            requestsPerHour = 10,
            message = "User registration rate limit exceeded. Please try again later."
    )
    ResponseEntity<ModuleResponse> registerUser(
            @Parameter(name = "email", required = true, example = "someone@example.com") @RequestParam String email,
            @Parameter(name = "password", required = true, example = "Hello@1234") @RequestParam String password
    ) {
        return LoggedResponse.call(
                ()-> authService.registerUser(email, password),
                "Register User",
                log
        );
    }

    @Operation(
            summary = "Verify Registration OTP",
            description = "Verifies the OTP sent to the user's email during registration.",
            responses = {
                    @ApiResponse(
                            description = "OTP verified successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/register-otp")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 10,
            message = "OTP Verification rate limit exceeded. Please try again later."
    )
    ResponseEntity<ModuleResponse> registerOtpVerification(
            @Parameter(name = "userId", required = true, example = "UA000001") @RequestParam String userId,
            @Parameter(name = "otp", required = true, example = "1234") @RequestParam String otp
    ) {
        return LoggedResponse.call(
                ()-> authService.registerOtpVerification(userId, otp),
                "Register OTP Verification",
                log
        );
    }

    @Operation(
            summary = "Refresh Authentication Token",
            description = "Refreshes the JWT authentication token for a user using OTP verification.",
            responses = {
                    @ApiResponse(
                            description = "OTP verified successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/refresh-token")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 20,
            message = "Refresh token rate limit exceeded. Please try again later."
    )
    ResponseEntity<?> refreshToken() {
        return LoggedResponse.call(
                ()-> authService.refreshToken(),
                "Refresh Token",
                log
        );
    }

    @Operation(
            summary = "Logout User",
            description = "Logs out the currently authenticated user by invalidating their session or token.",
            responses = {
                    @ApiResponse(
                            description = "User logged out successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Void.class)
                            )
                    )
            }
    )
    @PostMapping("/logout")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 20,
            message = "Logout rate limit exceeded. Please try again later."
    )
    ResponseEntity<?> logout() {
        return LoggedResponse.call(
                ()-> authService.logout(),
                "Logout",
                log
        );
    }

    @Operation(
            summary = "Forgot Password OTP",
            description = "Sends a forgot password OTP to the user's email.",
            responses = {
                    @ApiResponse(
                            description = "OTP sent successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ModuleResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/forgot-password-otp")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 10,
            message = "OTP Sent for Forgot Password"
    )
    ResponseEntity<ModuleResponse> forgotPasswordOtp(@Parameter(name = "email", required = true, example = "someone@example.com") @RequestParam String email) {
        return LoggedResponse.call(
                ()-> authService.forgotPasswordOtp(email),
                "Forgot Password OTP",
                log
        );
    }

    @Operation(
            summary = "Forgot Password",
            description = "Verifies the OTP sent to the user for password reset.",
            responses = {
                    @ApiResponse(
                            description = "OTP verified successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ModuleResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/forgot-password")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 10,
            message = "OTP Sent for Forgot Password"
    )
    ResponseEntity<ModuleResponse> forgotPassword(@Parameter(name = "userId", required = true, example = "UA000001") @RequestParam String userId,
                                                  @Parameter(name = "otp", required = true, example = "1234") @RequestParam String otp,
                                                  @Parameter(name = "password", required = true, example = "Hello@1234") @RequestParam String password) {
        ForgotPasswordRequest forgotPasswordRequest = ForgotPasswordRequest.builder()
                .userId(userId)
                .otp(otp)
                .password(password)
                .build();
        return LoggedResponse.call(
                ()-> authService.forgotPassword(forgotPasswordRequest),
                "Forgot Password",
                log
        );
    }

    @Operation(
            summary = "Get Registration Status",
            description = "Retrieves the registration status of the current user.",
            responses = {
                    @ApiResponse(
                            description = "Registration status retrieved successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RegistrationResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/registration-status")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 10,
            requestsPerHour = 100,
            message = "Request limit exceeded. Please try again later."
    )
    ResponseEntity<RegistrationResponse> getRegistrationStatus(@RequestParam String email) {
        return LoggedResponse.call(
                ()-> authService.getRegistrationStatus(email),
                "Get Registration Status",
                log
        );
    }
}