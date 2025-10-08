package com.revquix.backend.auth.authentication;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: CustomUserDetailsService
 */

import com.revquix.backend.application.constants.CacheConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.AuthenticationException;
import com.revquix.backend.application.service.CacheService;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.enums.EntrypointType;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.util.EntrypointTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("{}::loadUserByUsername -> Loading user by username: {}", getClass().getSimpleName(), username);
        EntrypointType entrypointType = EntrypointTypeUtil.parse(username);
        ErrorData errorData;
        if (entrypointType.equals(EntrypointType.username)) errorData = ErrorData.NO_USER_WITH_USERNAME;
        else errorData = ErrorData.NO_USER_WITH_EMAIL;
        UserAuth userAuth = userAuthRepository.findByEntrypoint(username)
                .orElseThrow(() -> new AuthenticationException(errorData));
        String key = cacheService.generateKey(CacheConstants.USER_BY_ID_PREFIX, userAuth.getUserId());
        cacheService.put(key, userAuth);
        return UserIdentity.create(userAuth);
    }
}
