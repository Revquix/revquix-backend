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
package com.revquix.backend.auth.listener;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: RegisterUserOtpEventListener
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.auth.events.RegisterUserOtpEvent;
import com.revquix.backend.auth.processor.RegistrationOtpProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterUserOtpEventListener {

    private final RegistrationOtpProcessor registrationOtpProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void onEvent(RegisterUserOtpEvent event) {
        if (event == null) {
            log.warn("{}::onEvent -> Received null event, skipping processing.", this.getClass().getSimpleName());
            return;
        }
        String breadcrumbId = event.getBreadcrumbId();
        try {
            MDC.put(ServiceConstants.BREADCRUMB_ID, breadcrumbId);
            log.info("{}::onEvent -> RegisterUserOtpEvent received: {}, breadcrumbId: {}", this.getClass().getSimpleName(), event.getUserAuth().getEmail(), breadcrumbId);
            registrationOtpProcessor.process(event.getUserAuth());
        } finally {
            MDC.clear();
        }
    }
}
