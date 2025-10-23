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
package com.revquix.backend.application.utils;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: ServletUtil
 */

import com.revquix.backend.application.constants.ServiceConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServletUtil {

    private final HttpServletRequest httpServletRequest;

    public String browser() {
        log.info("{}::browser -> Fetching browser from request", getClass().getSimpleName());
        String header = httpServletRequest.getHeader(ServiceConstants.REVQUIX_WEB_BROWSER);
        if (header == null || header.isEmpty() || header.isBlank()) {
            log.info("{}::browser -> Browser header is missing or empty", getClass().getSimpleName());
            return ServiceConstants.DEFAULT_BROWSER;
        }
        return header;
    }

    public String os() {
        log.info("{}::os -> Fetching os from request", getClass().getSimpleName());
        String header = httpServletRequest.getHeader(ServiceConstants.REVQUIX_WEB_OS);
        if (header == null || header.isEmpty() || header.isBlank()) {
            log.info("{}::os -> OS header is missing or empty", getClass().getSimpleName());
            return ServiceConstants.DEFAULT_OS;
        }
        return header;
    }
}
