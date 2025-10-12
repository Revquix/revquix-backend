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
  Project: revquix-sm
  GitHub: github.com/rohit-zip
  File: UserAuthCacheProvider
 */

import com.revquix.backend.application.constants.CacheConstants;
import com.revquix.backend.application.service.CacheService;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.model.UserAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthCache {

    private final CacheService cacheService;
    private final UserAuthRepository userAuthRepository;

    public Optional<UserAuth> findById(String id) {
        log.info("{}::findById -> Fetching user auth details for id: {}", this.getClass().getSimpleName(), id);
        String key = cacheService.generateKey(CacheConstants.USER_BY_ID_PREFIX, id);
        UserAuth userAuth;
        userAuth = cacheService.get(key, UserAuth.class);
        if (userAuth == null) {
            Optional<UserAuth> byId = userAuthRepository.findById(id);
            if (byId.isEmpty()) {
                return Optional.empty();
            }
            userAuth = byId.get();
            cacheService.put(key, userAuth);
        }
        return Optional.of(userAuth);
    }

    public void put(UserAuth userAuth) {
        log.info("{}::put -> Caching user auth details for id: {}", this.getClass().getSimpleName(), userAuth.getUserId());
        String key = cacheService.generateKey(CacheConstants.USER_BY_ID_PREFIX, userAuth.getUserId());
        cacheService.put(key, userAuth);
    }

    public void deleteById(String userId) {
        log.info("{}::deleteById -> Deleting user auth cache for id: {}", this.getClass().getSimpleName(), userId);
        String key = cacheService.generateKey(CacheConstants.USER_BY_ID_PREFIX, userId);
        cacheService.delete(key);
    }
}
