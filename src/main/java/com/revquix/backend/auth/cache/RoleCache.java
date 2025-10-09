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
package com.revquix.backend.auth.cache;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: RleCacheProvider
 */

import com.revquix.backend.application.constants.CacheConstants;
import com.revquix.backend.application.service.CacheService;
import com.revquix.backend.auth.dao.repository.RoleRepository;
import com.revquix.backend.auth.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleCache {

    private final CacheService cacheService;
    private final RoleRepository roleRepository;

    public Optional<Role> findById(String id) {
        log.info("{}::getRole -> Fetching role details for id: {}", this.getClass().getSimpleName(), id);
        String key = cacheService.generateKey(CacheConstants.ROLE_BY_ID_PREFIX, id);
        Role role = cacheService.get(key, Role.class);
        if (role != null) {
            return Optional.of(role);
        }
        return roleRepository.findById(id).map(r -> {
            cacheService.put(key, r);
            return r;
        });
    }
}
