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
  File: UserAuthController
 */

import com.revquix.backend.application.annotation.RateLimit;
import com.revquix.backend.application.enums.RateLimitType;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.utils.LoggedResponse;
import com.revquix.backend.auth.payload.request.MfaRequest;
import com.revquix.backend.auth.payload.response.ModuleResponse;
import com.revquix.backend.auth.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user-auth")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "User Authentication Controller",
        description = "APIs related to user management processes."
)
@ApiResponses(value = {
        @ApiResponse(
                description = "Unauthorized access.",
                responseCode = "401",
                content = @Content(schema = @Schema(implementation = Void.class))
        ),
        @ApiResponse(
                description = "Access forbidden.",
                responseCode = "403",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
        ),
        @ApiResponse(
                description = "Invalid request parameters.",
                responseCode = "400",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
        ),
        @ApiResponse(
                description = "Internal server error.",
                responseCode = "500",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
        )
})
public class UserAuthController {

    private static final Logger log = LoggerFactory.getLogger(UserAuthController.class);
    private final UserAuthService userAuthService;

    @Operation(
            summary = "Toggle Multi-Factor Authentication (MFA)",
            description = "Enables or disables Multi-Factor Authentication for the authenticated user.",
            responses = {
                    @ApiResponse(
                            description = "MFA toggled successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ModuleResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/toggle-mfa")
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 1,
            requestsPerHour = 1,
            message = "You can toggle MFA only once per hour."
    )
    ResponseEntity<ModuleResponse> toggleMfa(@RequestBody MfaRequest mfaRequest) {
        return LoggedResponse.call(
                ()-> userAuthService.toggleMfa(mfaRequest),
                "Toggle MFA",
                log
        );
    }
}
