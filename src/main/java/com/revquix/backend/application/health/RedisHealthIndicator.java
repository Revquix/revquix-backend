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
package com.revquix.backend.application.health;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: RedisHealthIndicator
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            if (connection != null) {
                connection.ping();
                connection.close();
                log.info("RedisHealth :: Redis seems to be healthy");
                return Health.up()
                        .withDetail("redis", "Available")
                        .withDetail("database", getDatabaseInfo())
                        .build();
            } else {
                log.error("RedisHealth :: No connection available");
                return Health.down().withDetail("redis", "Connection is null").build();
            }
        } catch (Exception exception) {
            log.error("RedisHealth :: Redis seems to be unhealthy", exception);
            return Health.down()
                    .withDetail("redis", "Not reachable")
                    .withException(exception)
                    .build();
        }
    }

    private String getDatabaseInfo() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            String info = new String(connection.info("server").toString());
            connection.close();
            return info.split("\r\n")[1]; // Get Redis version
        } catch (Exception e) {
            return "Unable to retrieve database info";
        }
    }
}
