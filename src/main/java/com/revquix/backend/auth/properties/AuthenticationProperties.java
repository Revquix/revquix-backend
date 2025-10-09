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
    private OtpInfo otpInfo;

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

    @Getter
    @Setter
    public static class OtpInfo {
        private Registration registration = new Registration();
        private PasswordReset passwordReset;
    }

    @Getter
    @Setter
    public static class Registration {
        private boolean mailEnabled = true;
        private int otpExpiryMinutes = 10;
        private int maxOtpRequests = 5;
        private boolean logEnabled = false;
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private boolean mailEnabled = true;
        private int otpExpiryMinutes = 10;
        private int maxOtpRequests = 5;
        private boolean logEnabled = false;
    }
}
