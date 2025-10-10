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
package com.revquix.backend.application.interceptors;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: MDCFilter
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.utils.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class MDCFilter extends OncePerRequestFilter {

    private final IpUtils ipUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("MDCFilter::doFilterInternal -> Initialized MDC Filter");
        String breadcrumbId = request.getHeader(ServiceConstants.BREADCRUMB_ID);
        if (Objects.isNull(breadcrumbId) || breadcrumbId.isEmpty()) {
            log.debug("MDCFilter::doFilterInternal -> Breadcrumb Id is not present in the request. Auto Generating the breadcrumbId", getClass().getSimpleName());
            breadcrumbId = UUID.randomUUID().toString();
        }
        MDC.put(ServiceConstants.BREADCRUMB_ID, breadcrumbId);
        MDC.put(ServiceConstants.REMOTE_ADDRESS, ipUtils.getIpv4());
        MDC.put(ServiceConstants.REQUEST_URI, request.getRequestURI());
        MDC.put(ServiceConstants.HTTP_METHOD, request.getMethod());

        try {
            response.setHeader(ServiceConstants.BREADCRUMB_ID, breadcrumbId);
            filterChain.doFilter(request, response);
        } finally {
            log.debug("MDCFilter::doFilterInternal -> Removing MDC entry data");
            MDC.clear();
        }
    }
}
