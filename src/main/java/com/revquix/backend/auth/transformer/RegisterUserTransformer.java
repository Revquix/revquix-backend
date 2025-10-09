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
package com.revquix.backend.auth.transformer;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: RegisterUserTransformer
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.InternalServerException;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.cache.RoleCache;
import com.revquix.backend.auth.dao.repository.RoleRepository;
import com.revquix.backend.auth.model.Role;
import com.revquix.backend.auth.model.UserAuth;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterUserTransformer {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IpUtils ipUtils;
    private final RoleCache roleCache;

    private List<Role> allRoles;

    @PostConstruct
    private void init() {
        allRoles = roleRepository.findAll();
        log.info("{}::init -> Cached all roles: {}", this.getClass().getSimpleName(), allRoles);
    }

    public UserAuth transform(String email, String password) {
        log.info("{}::transform -> Transforming registration data for email: {}", this.getClass().getSimpleName(), email);
        List<Role> roles = new ArrayList<>();
        populateRoles(email, roles);
        UserAuth userAuth = UserAuth
                .builder()
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .isEmailVerified(false)
                .authProvider(List.of(ServiceConstants.LOCAL))
                .registerIp(ipUtils.getIpv4())
                .roles(roles)
                .build();
        log.info("{}::transform -> Transformed UserAuth: {}", this.getClass().getSimpleName(), userAuth.toJson());
        return userAuth;
    }

    private void populateRoles(String email, List<Role> roles) {
        if (email.endsWith("@revquix.com")) {
            roles.addAll(allRoles);
        } else {
            roleCache.findById("user")
                    .ifPresentOrElse(
                            roles::add,
                            ()-> {
                                throw new InternalServerException(ErrorData.USER_ROLE_NOT_FOUND);
                            }
                    );
        }
    }
}
