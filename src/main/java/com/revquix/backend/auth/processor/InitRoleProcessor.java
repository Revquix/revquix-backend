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
package com.revquix.backend.auth.processor;

import com.revquix.backend.auth.dao.repository.RoleRepository;
import com.revquix.backend.auth.model.Role;
import com.revquix.backend.auth.payload.RolePayload;
import com.revquix.backend.auth.properties.FetchRoleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitRoleProcessor {

    private final FetchRoleProperties fetchRoleProperties;
    private final RoleRepository roleRepository;

    public void process() {
        log.info("{}::process -> Role Processor Initiated", getClass().getSimpleName());
        Map<String, RolePayload> roles = fetchRoleProperties.data;
        if (roles.isEmpty()) return;
        List<Role> rolesToSave = roles
                .values()
                .stream()
                .map(rolePayload -> Role
                        .builder()
                        .roleId(rolePayload.getId())
                        .role(rolePayload.getRole())
                        .internalRoles(Objects.isNull(rolePayload.getInternalRoles()) ? null : rolePayload.getInternalRoles())
                        .description(rolePayload.getDescription())
                        .build()
                )
                .toList();
        roleRepository.saveAll(rolesToSave);
    }
}
