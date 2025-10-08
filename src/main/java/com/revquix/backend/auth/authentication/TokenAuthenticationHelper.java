package com.revquix.backend.auth.authentication;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: TokenAuthenticationHelper
 */

import com.revquix.backend.application.constants.CacheConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.service.CacheService;
import com.revquix.backend.application.utils.OutputStreamExceptionGenerator;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import com.revquix.backend.auth.util.TokenExtractorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationHelper {

    private final JwtDecoder jwtDecoder;
    private final JwtDataProvider jwtDataProvider;
    private final CacheService cacheService;
    private final UserAuthRepository userAuthRepository;

    public UserIdentity authenticate(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        log.debug("TokenAuthenticationHelper::authenticate -> Authenticating token from request");
        Jwt jwt = jwtDecoder.decode(TokenExtractorUtil.extractToken(request));
        String userId = jwtDataProvider.extractUserId(jwt);
        String key = cacheService.generateKey(CacheConstants.USER_BY_ID_PREFIX, userId);
        UserAuth userAuth;
        userAuth = cacheService.get(key, UserAuth.class);
        if (userAuth == null) {
            Optional<UserAuth> byId = userAuthRepository.findById(userId);
            if (byId.isEmpty()) {
                log.error("TokenAuthenticationHelper::authenticate -> User not found with id: {}", userId);
                OutputStreamExceptionGenerator.generateExceptionResponse(ErrorData.USER_NOT_FOUND_FOR_GIVEN_TOKEN, HttpStatus.UNAUTHORIZED, httpServletResponse);
                return null;
            }
            userAuth = byId.get();
            cacheService.put(key, userAuth);
        }
        return UserIdentity.create(userAuth);
    }
}
