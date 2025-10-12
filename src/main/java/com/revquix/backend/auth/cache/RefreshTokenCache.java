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
import com.revquix.backend.auth.dao.repository.RefreshTokenRepository;
import com.revquix.backend.auth.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCache {

    private final CacheService cacheService;
    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findById(String id) {
        log.info("{}::findById -> Fetching refresh token details for id: {}", this.getClass().getSimpleName(), id);
        String key = cacheService.generateKey(CacheConstants.REFRESH_TOKEN_BY_ID_PREFIX, id);
        RefreshToken refreshToken;
        refreshToken = cacheService.get(key, RefreshToken.class);
        if (refreshToken == null) {
            Optional<RefreshToken> byId = refreshTokenRepository.findById(id);
            if (byId.isEmpty()) {
                return Optional.empty();
            }
            refreshToken = byId.get();
            cacheService.put(key, refreshToken);
        }
        return Optional.of(refreshToken);
    }

    public void put(RefreshToken refreshToken) {
        log.info("{}::put -> Caching refresh token details for id: {}", this.getClass().getSimpleName(), refreshToken.getJti());
        String key = cacheService.generateKey(CacheConstants.REFRESH_TOKEN_BY_ID_PREFIX, refreshToken.getJti());
        cacheService.put(key, refreshToken);
    }

    public void deleteById(String jti) {
        log.info("{}::deleteById -> Deleting refresh token cache for id: {}", this.getClass().getSimpleName(), jti);
        String key = cacheService.generateKey(CacheConstants.REFRESH_TOKEN_BY_ID_PREFIX, jti);
        cacheService.delete(key);
    }
}
