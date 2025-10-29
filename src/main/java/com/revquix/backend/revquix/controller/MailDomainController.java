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
package com.revquix.backend.revquix.controller;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: DomainController
 */

import com.revquix.backend.application.annotation.RateLimit;
import com.revquix.backend.application.enums.RateLimitType;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.utils.LoggedResponse;
import com.revquix.backend.auth.payload.request.TokenRequest;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.service.AuthService;
import com.revquix.backend.revquix.payload.request.AddMailDomainRequest;
import com.revquix.backend.revquix.service.DomainService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/mail-domain")
@Tag(
        name = "Domain",
        description = "APIs for managing and retrieving domain-related information."
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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MailDomainController {

    private static final Logger log = LoggerFactory.getLogger(MailDomainController.class);
    private final DomainService domainService;

    @Operation(
            summary = "Add Mail Domain",
            description = "Adds a new mail domain to the system.",
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
    @PostMapping
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 20,
            requestsPerHour = 1000,
            message = "Too many requests to add domain. Please try again later."
    )
    ResponseEntity<AuthResponse> addDomain(@RequestBody AddMailDomainRequest addMailDomainRequest) {
        return LoggedResponse.call(
                ()-> domainService.addDomain(addMailDomainRequest),
                "Add Domain",
                log
        );
    }
}
