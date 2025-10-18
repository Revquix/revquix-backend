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
package com.revquix.backend.auth.authentication;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: CustomUserDetailsService
 */

import com.revquix.backend.application.constants.CacheConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.application.service.CacheService;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.enums.EntrypointType;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.util.EntrypointTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;
    private final CacheService cacheService;
    private final UserAuthCache userAuthcache;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("{}::loadUserByUsername -> Loading user by username: {}", getClass().getSimpleName(), username);
        EntrypointType entrypointType = EntrypointTypeUtil.parse(username);
        ErrorData errorData;
        if (entrypointType.equals(EntrypointType.username)) errorData = ErrorData.NO_USER_WITH_USERNAME;
        else errorData = ErrorData.NO_USER_WITH_EMAIL;
        UserAuth userAuth = userAuthRepository.findByEntrypoint(username)
                .orElseThrow(() -> new AuthenticationException(errorData));
        userAuthcache.put(userAuth);
        return UserIdentity.create(userAuth);
    }
}
