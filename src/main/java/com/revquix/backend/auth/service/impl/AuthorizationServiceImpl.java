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
package com.revquix.backend.auth.service.impl;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: AuthorizationServiceImpl
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.RoleRepository;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.guardrails.AssignRoleRequestValidator;
import com.revquix.backend.auth.guardrails.GenericUserValidator;
import com.revquix.backend.auth.model.Role;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.request.AssignRoleRequest;
import com.revquix.backend.auth.payload.response.AssignRoleResponse;
import com.revquix.backend.auth.service.AuthorizationService;
import com.revquix.backend.auth.util.IdentityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserAuthCache userAuthCache;
    private final UserAuthRepository userAuthRepository;
    private final RoleRepository roleRepository;
    private final AssignRoleRequestValidator assignRoleRequestValidator;

    @Override
    public ResponseEntity<AssignRoleResponse> assignRoles(AssignRoleRequest assignRoleRequest) {
        log.info("{}:: Assign Roles to User: {}", this.getClass().getSimpleName(), assignRoleRequest.toJson());
        assignRoleRequestValidator.validate(assignRoleRequest);
        UserAuth assigneeUserAuth = userAuthCache.findById(assignRoleRequest.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorData.ASSIGNEE_USER_NOT_FOUND));
        GenericUserValidator.validateThird(assigneeUserAuth);
        isRolesAlreadyPresent(assigneeUserAuth, assignRoleRequest);
        List<String> assignerRoles = IdentityProvider.getOrThrow().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (assignerRoles.size() == 1 && "user".equals(assignerRoles.get(0))) {
            throw new BadRequestException(ErrorData.ONLY_USER_ROLE_NOT_ALLOWED);
        }
        isAssignerHasRoles(assignerRoles, assignRoleRequest);
        for (String role : assignRoleRequest.getRoles()) {
            boolean hasRole = assigneeUserAuth
                    .getRoles()
                    .stream()
                    .anyMatch(roleData -> roleData.getRole().equals(role));
            if (!hasRole) {
                Role roleData = roleRepository.findByRole(role)
                        .orElseThrow(() -> new BadRequestException(ErrorData.ROLE_NOT_FOUND_NAME));
                assigneeUserAuth.getRoles().add(roleData);
            }
        }
        UserAuth updatedUserAuth = userAuthRepository.save(assigneeUserAuth);
        log.info("{}:: Roles assigned successfully to userId: {}", this.getClass().getSimpleName(), assignRoleRequest.getUserId());
        userAuthCache.put(updatedUserAuth);
        AssignRoleResponse assignRoleResponse = AssignRoleResponse
                .builder()
                .userId(updatedUserAuth.getUserId())
                .roles(updatedUserAuth.getRoles())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(assignRoleResponse);
    }

    private void isRolesAlreadyPresent(UserAuth userAuth, AssignRoleRequest assignRoleRequest) {
        log.info("{}::isRolesAlreadyPresent -> Checking if roles are already present for userId: {}", this.getClass().getSimpleName());
        List<String> currentRoles = userAuth.getRoles().stream().map(Role::getRole).toList();
        List<String> newRoles = assignRoleRequest.getRoles();
        if (currentRoles.containsAll(newRoles)) {
            throw new BadRequestException(ErrorData.ROLES_ALREADY_ASSIGNED);
        }
    }

    private void isAssignerHasRoles(List<String> assignerRoles, AssignRoleRequest assignRoleRequest) {
        log.info("{}::isAssignerHasRoles -> Checking if assigner has the roles to assign", this.getClass().getSimpleName());
        boolean assignerHasRoles = assignRoleRequest
                .getRoles()
                .stream()
                .allMatch(role -> assignerRoles.stream().anyMatch(role::equals));
        if (!assignerHasRoles) {
            throw new BadRequestException(ErrorData.ASSIGNER_NOT_HAVE_ROLES);
        }
    }
}