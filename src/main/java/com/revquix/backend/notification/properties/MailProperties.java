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
package com.revquix.backend.notification.properties;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: MailProperties
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mail")
@Getter
@Setter
public class MailProperties {

    private ZeptoMail zeptoMail;
    private MailDomain mailDomain;

    @Getter
    @Setter
    public static class ZeptoMail {
        private String apiUrl;
        private String apiKey;
        private int timeoutSeconds;

        private Registration registration;
        private ForgotPassword forgotPassword;
        private Mfa mfa;
    }

    @Getter
    @Setter
    public static class Registration {
        private String prefix;
        private String subject;
        private String template;
        private String context;
    }

    @Getter
    @Setter
    public static class ForgotPassword {
        private String prefix;
        private String subject;
        private String template;
        private String context;
    }

    @Getter
    @Setter
    public static class MailDomain {
        private boolean isEnabled = true;
        private List<String> allowedDomains;
    }

    @Getter
    @Setter
    public static class Mfa {
        private String prefix;
        private String subject;
        private String template;
        private String context;
    }
}
