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
  Project: revquix-sm
  GitHub: github.com/rohit-zip
  File: RateLimitInterceptor
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revquix.backend.application.annotation.RateLimit;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.payload.RateLimitResult;
import com.revquix.backend.application.service.RateLimitService;
import com.revquix.backend.application.utils.MdcUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = getRateLimitAnnotation(handlerMethod);

        if (rateLimit == null) {
            return applyDefaultRateLimit(request, response);
        }

        return applyCustomRateLimit(request, response, rateLimit, handlerMethod);
    }

    private RateLimit getRateLimitAnnotation(HandlerMethod handlerMethod) {
        RateLimit methodRateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (methodRateLimit != null) {
            return methodRateLimit;
        }
        return handlerMethod.getBeanType().getAnnotation(RateLimit.class);
    }

    private boolean applyDefaultRateLimit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ipAddress = getClientIpAddress(request);
        RateLimitResult ipResult = rateLimitService.checkIpRateLimit(ipAddress);

        if (!ipResult.isAllowed()) {
            handleRateLimitExceeded(response, ipResult, "Default IP rate limit exceeded");
            return false;
        }

        addRateLimitHeaders(response, ipResult);
        return true;
    }

    private boolean applyCustomRateLimit(HttpServletRequest request, HttpServletResponse response,
                                         RateLimit rateLimit, HandlerMethod handlerMethod) throws IOException {

        if (shouldSkipRateLimit(request, rateLimit, handlerMethod)) {
            log.debug("Rate limit skipped for endpoint: {}", handlerMethod.getMethod().getName());
            return true;
        }

        String ipAddress = getIdentifier(request, rateLimit, handlerMethod);

        // Only IP-based rate limiting
        RateLimitResult result = rateLimitService.checkRateLimit(ipAddress, rateLimit.requestsPerMinute(), 60);

        if (!result.isAllowed()) {
            handleRateLimitExceeded(response, result, rateLimit.message());
            return false;
        }

        // Also check hour limit
        RateLimitResult hourResult = rateLimitService.checkRateLimit(ipAddress, rateLimit.requestsPerHour(), 3600);
        if (!hourResult.isAllowed()) {
            handleRateLimitExceeded(response, hourResult, rateLimit.message() + " (hourly)");
            return false;
        }

        addRateLimitHeaders(response, result);
        return true;
    }

    private String getIdentifier(HttpServletRequest request, RateLimit rateLimit, HandlerMethod handlerMethod) {
        if (StringUtils.hasText(rateLimit.identifier())) {
            return evaluateSpelExpression(rateLimit.identifier(), request, handlerMethod);
        }

        // Always return IP address since we only support IP-based rate limiting
        return getClientIpAddress(request);
    }

    private String evaluateSpelExpression(String expression, HttpServletRequest request, HandlerMethod handlerMethod) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("request", request);
            context.setVariable("method", handlerMethod);
            context.setVariable("ip", getClientIpAddress(request));

            return expressionParser.parseExpression(expression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("Error evaluating SpEL expression: {}", expression, e);
            return getClientIpAddress(request);
        }
    }

    private boolean shouldSkipRateLimit(HttpServletRequest request, RateLimit rateLimit, HandlerMethod handlerMethod) {
        if (!StringUtils.hasText(rateLimit.skipCondition())) {
            return false;
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("request", request);
            context.setVariable("method", handlerMethod);
            context.setVariable("ip", getClientIpAddress(request));

            // Simple admin check via header
            boolean isAdmin = "true".equals(request.getHeader("X-Admin"));
            context.setVariable("isAdmin", isAdmin);

            Boolean result = expressionParser.parseExpression(rateLimit.skipCondition()).getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error evaluating skip condition: {}", rateLimit.skipCondition(), e);
            return false;
        }
    }

    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitResult result, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ExceptionResponse exceptionResponse = ExceptionResponse
                .builder()
                .message(message)
                .code(ErrorData.RATE_LIMIT_EXCEEDED.getCode())
                .breadcrumbId(MdcUtils.getBreadcrumbId())
                .localizedMessage(String.format("Rate limit exceeded. for type %s, remaining requests %s. Try again at %s", result.getResetTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .errorType("Data Error")
                .httpStatus(HttpStatus.TOO_MANY_REQUESTS.name())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        log.warn("Rate limit exceeded: {}", message);
    }

    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult result) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getTotalRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTime().toEpochSecond(java.time.ZoneOffset.UTC)));
        response.setHeader("X-RateLimit-Type", result.getRateLimitType());
        response.setHeader("X-RateLimit-Window", String.valueOf(result.getWindowSizeSeconds()));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}