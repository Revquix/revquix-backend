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
package com.revquix.backend.application.config;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: AsyncTaskConfig
 */

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskConfig {

    @Value("${async.executor.core-pool-size:5}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${async.executor.await-termination-seconds:20}")
    private int awaitTerminationSeconds;

    @Value("${async.executor.thread-name-prefix:revquix-async-}")
    private String threadNamePrefix;

    private ThreadPoolTaskExecutor taskExecutor;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("{}::taskExecutor -> Creating EventTaskExecutor with corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
                getClass().getSimpleName(), corePoolSize, maxPoolSize, queueCapacity);

        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);

        // Use CallerRunsPolicy for production - prevents task rejection and provides backpressure
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Enable graceful shutdown
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Allow core threads to timeout for better resource management
        taskExecutor.setAllowCoreThreadTimeOut(true);

        taskExecutor.initialize();

        return taskExecutor;
    }

    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            log.info("{}::destroy -> Shutting down EventTaskExecutor gracefully", getClass().getSimpleName());
            taskExecutor.shutdown();
        }
    }
}
