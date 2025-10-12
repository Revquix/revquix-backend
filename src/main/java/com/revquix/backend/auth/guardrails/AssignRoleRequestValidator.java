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
  File: AssignRoleRequestValidator
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.auth.dao.repository.RoleRepository;
import com.revquix.backend.auth.model.Role;
import com.revquix.backend.auth.payload.request.AssignRoleRequest;
import com.revquix.backend.auth.util.IdentityProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignRoleRequestValidator {

    private final RoleRepository roleRepository;

    private List<Role> allRoles;

    @PostConstruct
    private void init() {
        allRoles = roleRepository.findAll();
        log.info("{}::init -> Cached all roles: {}", this.getClass().getSimpleName(), allRoles);
    }

    public void validate(AssignRoleRequest assignRoleRequest) {
        log.info("{}:: Validating AssignRoleRequest: {}", AssignRoleRequestValidator.class.getSimpleName(), assignRoleRequest.toJson());
        if (assignRoleRequest.getUserId().equals(IdentityProvider.getOrThrow().getUserId())) {
            throw new BadRequestException(ErrorData.CANNOT_ASSIGN_ROLE_TO_SELF);
        }
        if (assignRoleRequest.getUserId() == null || assignRoleRequest.getUserId().isEmpty()) {
            throw new BadRequestException(ErrorData.USER_ID_NULL);
        }
        if (assignRoleRequest.getRoles() == null || assignRoleRequest.getRoles().isEmpty()) {
            throw new BadRequestException(ErrorData.ROLES_EMPTY);
        }
        if (assignRoleRequest.getRoles().contains(null)) {
            throw new BadRequestException(ErrorData.ROLE_LIST_CONTAINS_NULL);
        }
        allRolesValid(assignRoleRequest.getRoles(), allRoles.stream().map(Role::getRole).toList());
        if (assignRoleRequest.getRoles().contains("admin")) {
            throw new BadRequestException(ErrorData.ADMIN_ROLE_CANNOT_BE_ASSIGNED_THROUGH_THIS_API);
        }
    }

    private void allRolesValid(List<String> givenRoles, List<String> parentRoles) {
        Set<String> parentRoleSet = new HashSet<>(parentRoles);
        List<String> invalidRoles = givenRoles.stream()
                .filter(role -> !parentRoleSet.contains(role))
                .toList();

        if (!invalidRoles.isEmpty()) {
            throw new BadRequestException(ErrorData.INVALID_ROLES_PROVIDED);
        }
    }
}
