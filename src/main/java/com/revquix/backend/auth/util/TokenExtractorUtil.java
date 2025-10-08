package com.revquix.backend.auth.util;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: TokenExtractorUtil
 */

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class TokenExtractorUtil {

    public static String extractToken(HttpServletRequest request) {
        log.debug("TokenExtractorUtil::extractToken -> Extracting token from bearer token");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        log.debug("TokenExtractorUtil::extractToken -> No token found");
        return null;
    }
}
