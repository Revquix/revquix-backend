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
  File: JwtAuthenticationFilter
 */

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.payload.OutputStreamErrorPayload;
import com.revquix.backend.application.utils.OutputStreamUtil;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.util.TokenExtractorUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final TokenAuthenticationHelper tokenAuthenticationHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter::doFilterInternal -> Inside JWT Authentication Filter");
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = TokenExtractorUtil.extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            jwtDecoder.decode(token);
        } catch (JwtValidationException exception) {
            handleJwtValidationException(exception, response);
            return;
        } catch (Exception exception) {
            handleException(exception, response);
            return;
        }
        UserIdentity userIdentity = tokenAuthenticationHelper.authenticate(request, response);
        if (userIdentity == null) {
            filterChain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userIdentity,
                null,
                userIdentity.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void handleJwtValidationException(JwtValidationException exception, HttpServletResponse response) throws IOException {
        log.error("JwtAuthenticationFilter::handleJwtValidationException -> JWT Validation Exception occurred", exception);
        Collection<OAuth2Error> errors = exception.getErrors();
        boolean isExpired = false;
        for (OAuth2Error error : errors) {
            if (error.getDescription().contains("expired")) {
                isExpired = true;
                break;
            }
        }
        ExceptionResponse exceptionResponse = ExceptionResponse
                .builder()
                .code(
                        isExpired ? ErrorData.TOKEN_EXPIRED.getCode() : ErrorData.MALFORMED_TOKEN.getCode())
                .message(
                        isExpired ? ErrorData.TOKEN_EXPIRED.getMessage() : ErrorData.MALFORMED_TOKEN.getMessage()
                )
                .breadcrumbId(MDC.get(ServiceConstants.BREADCRUMB_ID))
                .isTokenExpired(isExpired)
                .localizedMessage(exception.getMessage())
                .build();
        OutputStreamUtil.getOutputStream(new OutputStreamErrorPayload(HttpStatus.FORBIDDEN, exceptionResponse, response));
    }

    private void handleException(Exception exception, HttpServletResponse response) throws IOException {
        log.error("JwtAuthenticationFilter::handleException -> Exception Occurred", exception);
        ExceptionResponse exceptionResponse = ExceptionResponse
                .builder()
                .code(ErrorData.MALFORMED_TOKEN.getCode())
                .message(ErrorData.MALFORMED_TOKEN.getMessage())
                .breadcrumbId(MDC.get(ServiceConstants.BREADCRUMB_ID))
                .isTokenExpired(false)
                .localizedMessage(exception.getMessage())
                .build();
        OutputStreamUtil.getOutputStream(new OutputStreamErrorPayload(HttpStatus.UNAUTHORIZED, exceptionResponse, response));
    }
}
