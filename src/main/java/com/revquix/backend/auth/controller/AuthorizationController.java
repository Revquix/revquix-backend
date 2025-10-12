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
  File: AuthorizationController
 */

import com.revquix.backend.application.annotation.RateLimit;
import com.revquix.backend.application.enums.RateLimitType;
import com.revquix.backend.application.utils.LoggedResponse;
import com.revquix.backend.auth.payload.request.AssignRoleRequest;
import com.revquix.backend.auth.payload.response.AssignRoleResponse;
import com.revquix.backend.auth.payload.response.AuthResponse;
import com.revquix.backend.auth.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);
    private final AuthorizationService authorizationService;

    @Operation(
            summary = "Assign Roles to User",
            description = "Assigns one or more roles to a user, updating their permissions and access levels within the system.",
            responses = {
                    @ApiResponse(
                            description = "Roles assigned successfully.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    )
            }
    )
    @RateLimit(
            type = RateLimitType.IP_BASED,
            requestsPerMinute = 4,
            requestsPerHour = 20,
            message = "Too many role assignment requests, please try again later."
    )
    @PostMapping("/assign-roles")
    ResponseEntity<AssignRoleResponse> assignRoles(@RequestBody AssignRoleRequest assignRoleRequest) {
        return LoggedResponse.call(
                ()-> authorizationService.assignRoles(assignRoleRequest),
                "Assign Roles",
                logger
        );
    }
}
