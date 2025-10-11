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
  File: OriginValidator
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.application.exception.payload.InternalServerException;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstanceValidator {

    private final AuthenticationProperties authenticationProperties;
    private final Environment environment;

    private String activeProfile;

    @PostConstruct
    public void init() {
        log.info("{}::init -> Initializing Instance Validator", getClass().getSimpleName());
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length > 1) {
            throw new InternalServerException(ErrorData.MULTIPLE_ACTIVE_PROFILES_FOUND);
        }
        if (profiles.length == 0) {
            throw new InternalServerException(ErrorData.NO_ACTIVE_PROFILE_FOUND);
        }
        this.activeProfile = profiles[0];
        log.info("{}::init -> Active Profile set to: {}", getClass().getSimpleName(), this.activeProfile);
    }

    public void validate(UserIdentity userIdentity) {
        log.info("{}::validate -> Validating Instance for email: {}", getClass().getSimpleName(), userIdentity.getEmail());
        Collection<? extends GrantedAuthority> authorities = userIdentity.getAuthorities();
        validateInstances(authorities);
    }

    private void validateInstances(Collection<? extends GrantedAuthority> authorities) {
        log.info("{}::validateInstances -> Validating Instances for roles: {}", getClass().getSimpleName(), authorities);
        boolean isPresent = false;
        Map<String, AuthenticationProperties.RoleToAllow> rolesToAllow = authenticationProperties.getAuthorization().getRolesToAllow();
        for (String instance : rolesToAllow.keySet()) {
            AuthenticationProperties.RoleToAllow requiredRoles = rolesToAllow.get(instance);
            if (requiredRoles.getInstance().equals(activeProfile)) {
                isPresent = checkAllRolesContain(authorities, requiredRoles.getMustRoles());
                break;
            }
        }
        if (!isPresent) {
            throw new AuthenticationException(ErrorData.REQUIRED_ROLES_NOT_PRESENT, HttpStatus.FORBIDDEN);
        }
    }

    private boolean checkAllRolesContain(Collection<? extends GrantedAuthority> authorities, List<String> mustRoles) {
        List<String> userRoles = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String element : userRoles) {
            frequencyMap.put(element, frequencyMap.getOrDefault(element, 0) + 1);
        }
        for (String element : mustRoles) {
            if (frequencyMap.containsKey(element) && frequencyMap.get(element) > 0) {
                frequencyMap.put(element, frequencyMap.getOrDefault(element, 0) - 1);
            } else {
                return false;
            }
        }
        return true;
    }


}
