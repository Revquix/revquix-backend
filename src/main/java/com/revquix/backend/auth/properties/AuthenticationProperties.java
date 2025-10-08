package com.revquix.backend.auth.properties;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: AuthenticationProperties
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "auth")
@Configuration
@Getter
@Setter
public class AuthenticationProperties {

    private KeyData keyData;
    private ExcludePaths excludePaths;
    private TokenInfo info;
    private Cors cors;

    @Getter
    @Setter
    public static class KeyData {
        private String path;
        private String password;
        private String alias;
    }

    @Getter
    @Setter
    public static class ExcludePaths {
        private List<String> paths;
    }

    @Getter
    @Setter
    public static class TokenInfo {
        private int accessTokenExpiryMinutes;
        private int refreshTokenExpiryDays;
        private String refreshTokenCookieName;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
        private long maxAge = 3600;
        private boolean allowCredentials = true;
    }
}
