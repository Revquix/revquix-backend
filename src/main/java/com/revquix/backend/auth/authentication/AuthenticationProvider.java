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
