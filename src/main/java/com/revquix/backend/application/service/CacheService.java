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
package com.revquix.backend.application.service;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: CacheService
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.ttl:3600}")
    private long defaultTtlSeconds;

    public <T> T get(String key, Class<T> type) {
        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue != null) {
                log.debug("{}::get -> Cache HIT for key: {}", getClass().getSimpleName(), key);
                return objectMapper.convertValue(cachedValue, type);
            }
            log.debug("{}::get -> Cache MISS for key: {}", getClass().getSimpleName(), key);
            return null;
        } catch (Exception e) {
            log.error("{}::get -> Error retrieving from cache for key '{}': {}", getClass().getSimpleName(), key, e.getMessage());
            return null;
        }
    }

    public void put(String key, Object value) {
        log.debug("{}::put -> Cache put for key: {}", getClass().getSimpleName(), key);
        put(key, value, Duration.ofSeconds(defaultTtlSeconds));
    }

    public void put(String key, Object value, Duration ttl) {
        log.debug("{}::put -> Cache put with duration for key: {}", getClass().getSimpleName(), key);
        try {
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, ttl);
                log.debug("{}::put -> Cached data for key '{}' with TTL: {}", getClass().getSimpleName(), key, ttl);
            } else {
                log.warn("{}::put -> Attempted to cache null value for key: {}", getClass().getSimpleName(), key);
            }
        } catch (Exception e) {
            log.error("{}::put -> Error caching data for key '{}': {}", getClass().getSimpleName(), key, e.getMessage());
        }
    }

    public boolean check(String key) {
        log.debug("{}::check -> Cache existence check for key: {}", getClass().getSimpleName(), key);
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("{}::check -> Cache existence check for key '{}': {}", getClass().getSimpleName(), key, exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("{}::check -> Error checking cache existence for key '{}': {}", getClass().getSimpleName(), key, e.getMessage());
            return false;
        }
    }

    public boolean delete(String key) {
        log.debug("{}::delete -> Cache delete for key: {}", getClass().getSimpleName(), key);
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("{}::delete -> Deleted cache entry for key: {}", getClass().getSimpleName(), key);
                return true;
            }
            log.debug("{}::delete -> Cache key '{}' was not found for deletion", getClass().getSimpleName(), key);
            return false;
        } catch (Exception e) {
            log.error("{}::delete -> Error deleting from cache for key '{}': {}", getClass().getSimpleName(), key, e.getMessage());
            return false;
        }
    }

    public String generateKey(String prefix, Object... keyParts) {
        log.info("{}::generateKey -> Generating cache key with prefix: {}", getClass().getSimpleName(), prefix);
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object part : keyParts) {
            keyBuilder.append(":").append(part.toString());
        }
        return keyBuilder.toString();
    }
}
