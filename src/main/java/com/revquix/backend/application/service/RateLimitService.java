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
  Project: revquix-sm
  GitHub: github.com/rohit-zip
  File: RateLimitService
 */

import com.revquix.backend.application.enums.RateLimitType;
import com.revquix.backend.application.payload.RateLimitResult;
import com.revquix.backend.application.properties.RateLimitProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    // Lua script for atomic sliding window rate limiting
    private static final String SLIDING_WINDOW_SCRIPT = """
        local key = KEYS[1]
        local window = tonumber(ARGV[1])
        local limit = tonumber(ARGV[2])
        local current_time = tonumber(ARGV[3])
        
        -- Remove expired entries
        redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window)
        
        -- Count current requests
        local current_requests = redis.call('ZCARD', key)
        
        if current_requests < limit then
            -- Add current request
            redis.call('ZADD', key, current_time, current_time)
            redis.call('EXPIRE', key, window)
            return {1, limit - current_requests - 1, current_requests + 1}
        else
            return {0, 0, current_requests}
        end
        """;

    private final DefaultRedisScript<List> slidingWindowScript = createSlidingWindowScript();

    private DefaultRedisScript<List> createSlidingWindowScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDING_WINDOW_SCRIPT);
        script.setResultType(List.class);
        return script;
    }

    /**
     * Check rate limit using sliding window algorithm (IP-based only)
     */
    public RateLimitResult checkRateLimit(String ipAddress, int limit, int windowSeconds) {
        if (!rateLimitProperties.isEnabled()) {
            return allowedResult(limit, windowSeconds);
        }

        String key = generateKey(ipAddress, windowSeconds);
        long currentTime = System.currentTimeMillis() / 1000;

        try {
            return executeSlidingWindow(key, limit, windowSeconds, currentTime);
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is down
            return allowedResult(limit, windowSeconds);
        }
    }

    /**
     * IP-based rate limiting with sliding window
     */
    public RateLimitResult checkIpRateLimit(String ipAddress) {
        if (!rateLimitProperties.getIpRateLimit().isEnabled()) {
            return allowedResult(100, 60);
        }

        // Check if IP is blocked
        if (isIpBlocked(ipAddress)) {
            return blockedResult();
        }

        RateLimitProperties.IpRateLimit ipConfig = rateLimitProperties.getIpRateLimit();

        // Check minute limit first
        RateLimitResult minuteResult = checkRateLimit(ipAddress, ipConfig.getRequestPerMinute(), 60);

        if (!minuteResult.isAllowed()) {
            // Block IP for configured duration
            blockIp(ipAddress, ipConfig.getBlockedDurationMinutes());
            log.warn("IP {} blocked due to minute rate limit exceeded", ipAddress);
            return minuteResult;
        }

        // Check hour limit
        RateLimitResult hourResult = checkRateLimit(ipAddress, ipConfig.getRequestPerHour(), 3600);

        if (!hourResult.isAllowed()) {
            // Block IP for configured duration
            blockIp(ipAddress, ipConfig.getBlockedDurationMinutes());
            log.warn("IP {} blocked due to hour rate limit exceeded", ipAddress);
        }

        return hourResult;
    }

    /**
     * Execute sliding window algorithm using Lua script
     */
    private RateLimitResult executeSlidingWindow(String key, int limit, int windowSeconds, long currentTime) {
        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(slidingWindowScript,
                Arrays.asList(key),
                windowSeconds, limit, currentTime);

        boolean allowed = result.get(0) == 1;
        long remaining = result.get(1);
        long total = result.get(2);

        LocalDateTime resetTime = LocalDateTime.ofEpochSecond(currentTime + windowSeconds, 0, ZoneOffset.UTC);

        log.debug("Sliding window result for key {}: allowed={}, remaining={}, total={}",
                key, allowed, remaining, total);

        return new RateLimitResult(allowed, remaining, total, windowSeconds, resetTime, RateLimitType.IP_BASED.getValue());
    }

    /**
     * Block IP address
     */
    private void blockIp(String ipAddress, int durationMinutes) {
        String blockKey = generateKey(ipAddress, "blocked");
        redisTemplate.opsForValue().set(blockKey, "blocked", durationMinutes, TimeUnit.MINUTES);
        log.warn("IP address blocked: {} for {} minutes", ipAddress, durationMinutes);
    }

    /**
     * Check if IP is blocked
     */
    private boolean isIpBlocked(String ipAddress) {
        String blockKey = generateKey(ipAddress, "blocked");
        boolean blocked = Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
        if (blocked) {
            log.debug("IP {} is currently blocked", ipAddress);
        }
        return blocked;
    }

    /**
     * Unblock IP address (admin function)
     */
    public boolean unblockIp(String ipAddress) {
        String blockKey = generateKey(ipAddress, "blocked");
        Boolean deleted = redisTemplate.delete(blockKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("IP address unblocked: {}", ipAddress);
            return true;
        }
        return false;
    }

    /**
     * Get IP block status
     */
    public BlockStatus getIpBlockStatus(String ipAddress) {
        String blockKey = generateKey(ipAddress, "blocked");
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            Long ttl = redisTemplate.getExpire(blockKey, TimeUnit.SECONDS);
            return new BlockStatus(true, ttl != null ? ttl : 0);
        }
        return new BlockStatus(false, 0);
    }

    /**
     * Generate Redis key for rate limiting
     */
    private String generateKey(String identifier, Object... suffixes) {
        StringBuilder keyBuilder = new StringBuilder("rate_limit:ip:")
                .append(identifier);

        for (Object suffix : suffixes) {
            keyBuilder.append(":").append(suffix);
        }

        return keyBuilder.toString();
    }

    /**
     * Helper methods
     */
    private RateLimitResult allowedResult(int limit, int windowSeconds) {
        return new RateLimitResult(true, limit - 1, 1, windowSeconds,
                LocalDateTime.now().plusSeconds(windowSeconds), RateLimitType.IP_BASED.getValue());
    }

    private RateLimitResult blockedResult() {
        return new RateLimitResult(false, 0, 0, 60,
                LocalDateTime.now().plusMinutes(1), RateLimitType.IP_BASED.getValue());
    }

    /**
     * Get rate limit statistics
     */
    public RateLimitStats getRateLimitStats(String ipAddress) {
        try {
            String minuteKey = generateKey(ipAddress, 60);
            String hourKey = generateKey(ipAddress, 3600);

            Long minuteCount = redisTemplate.opsForZSet().zCard(minuteKey);
            Long hourCount = redisTemplate.opsForZSet().zCard(hourKey);

            return new RateLimitStats(
                    ipAddress,
                    "ip",
                    minuteCount != null ? minuteCount : 0,
                    hourCount != null ? hourCount : 0,
                    redisTemplate.getExpire(minuteKey, TimeUnit.SECONDS),
                    redisTemplate.getExpire(hourKey, TimeUnit.SECONDS)
            );
        } catch (Exception e) {
            log.error("Error getting rate limit stats for IP {}", ipAddress, e);
            return new RateLimitStats(ipAddress, "ip", 0, 0, 0, 0);
        }
    }

    /**
     * Clear rate limit for IP
     */
    public boolean clearRateLimit(String ipAddress) {
        try {
            String minuteKey = generateKey(ipAddress, 60);
            String hourKey = generateKey(ipAddress, 3600);

            Long deleted = redisTemplate.delete(Arrays.asList(minuteKey, hourKey));
            log.info("Cleared rate limit for IP {}: {} keys deleted", ipAddress, deleted);
            return deleted != null && deleted > 0;
        } catch (Exception e) {
            log.error("Error clearing rate limit for IP {}", ipAddress, e);
            return false;
        }
    }

    @Data
    @AllArgsConstructor
    public static class RateLimitStats {
        private final String identifier;
        private final String type;
        private final long minuteCount;
        private final long hourCount;
        private final long minuteTtl;
        private final long hourTtl;
    }

    @Data
    @AllArgsConstructor
    public static class BlockStatus {
        private final boolean blocked;
        private final long remainingSeconds;
    }
}
