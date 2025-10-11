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
package com.revquix.backend.auth.util;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: CookieUtils
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenProvider {

    private final HttpServletRequest httpServletRequest;
    private final AuthenticationProperties authenticationProperties;

    public String get() {
        log.info("{}::get -> Fetching refresh token from cookies", getClass().getSimpleName());
        String refreshTokenCookieName = authenticationProperties.getInfo().getRefreshTokenCookieName();
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null || cookies.length == 0)
            throw new AuthenticationException(ErrorData.REFRESH_TOKEN_MISSING);
        Cookie cookie = getCookie(cookies, refreshTokenCookieName)
                .orElseThrow(() -> new AuthenticationException(ErrorData.REFRESH_TOKEN_MISSING));
        return cookie.getValue();
    }

    private static Optional<Cookie> getCookie(Cookie[] cookies, String name) {
        log.info("CookieUtils >> getCookie -> {}", name);
        if (Objects.nonNull(cookies) && cookies.length > 0) {
            return Optional.ofNullable(
                    Arrays
                        .stream(cookies)
                        .filter(cookie -> Objects.equals(cookie.getName(), name))
                        .findFirst()
                        .orElse(null)
            );
        }
        return Optional.empty();
    }
}
