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
  File: AuthenticationProvider
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.auth.payload.UserIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class AuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        return generateAuthentication(username, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(RevquixAuthenticationToken.class);
    }

    private UsernamePasswordAuthenticationToken generateAuthentication(String username, String password) {
        log.debug("{}::generateAuthentication -> Generating authentication token for username: {}", getClass().getSimpleName(), username);
        UserIdentity userIdentity = (UserIdentity) userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userIdentity.getPassword())) {
            throw new AuthenticationException(ErrorData.INCORRECT_PASSWORD);
        }
        validate(userIdentity);
        return new RevquixAuthenticationToken(userIdentity, null, userIdentity.getAuthorities());
    }

    private void validate(UserIdentity userIdentity) {
        if (!userIdentity.isEnabled()) {
            throw new AuthenticationException(ErrorData.USER_NOT_ENABLED);
        }
        if (!userIdentity.isAccountNonLocked()) {
            throw new AuthenticationException(ErrorData.ACCOUNT_LOCKED);
        }
        if (!userIdentity.isEmailVerified()) {
            throw new AuthenticationException(ErrorData.EMAIL_NOT_VERIFIED);
        }
    }
}
