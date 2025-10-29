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
package com.revquix.backend.notification.processor;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: SendMfaOtpMail
 */

import com.revquix.backend.application.payload.IPResponse;
import com.revquix.backend.application.service.IpService;
import com.revquix.backend.application.utils.DateUtil;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.application.utils.ServletUtil;
import com.revquix.backend.notification.payload.MfaOtpPayload;
import com.revquix.backend.notification.payload.RegistrationOtpPayload;
import com.revquix.backend.notification.payload.ZeptoMailResponse;
import com.revquix.backend.notification.properties.MailProperties;
import com.revquix.backend.notification.service.ZeptoMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendMfaOtpMail {

    private final MailProperties mailProperties;
    private final ZeptoMailService zeptoMailService;
    private final IpService ipService;
    private final IpUtils ipUtils;
    private final ServletUtil servletUtil;
    private final SpringTemplateEngine templateEngine;

    public void execute(MfaOtpPayload mfaOtpPayload) {
        log.info("{}::execute -> Sending MFA OTP mail to email: {}", getClass().getSimpleName(), mfaOtpPayload.getEmail());
        MailProperties.Mfa mfa = mailProperties.getZeptoMail().getMfa();
        build(mfaOtpPayload);
        ZeptoMailResponse zeptoMailResponse = zeptoMailService.send(
                mfa.getPrefix(),
                mfaOtpPayload.getEmail(),
                mfa.getSubject(),
                prepareContext(mfaOtpPayload),
                mfa.getName()
        );
        log.info("{}::execute -> OTP mail sent successfully with response: {}", getClass().getSimpleName(), zeptoMailResponse.toJson());
    }

    private String prepareContext(MfaOtpPayload mfaOtpPayload) {
        log.info("{}::prepareContext -> Preparing email context for Registration OTP mail", getClass().getSimpleName());
        MailProperties.Mfa mfa = mailProperties.getZeptoMail().getMfa();
        Context context = new Context();
        context.setVariable(mfa.getContext(), mfaOtpPayload);
        return templateEngine.process(mfa.getTemplate(), context);
    }

    private void build(MfaOtpPayload mfaOtpPayload) {
        log.info("{}::build -> Building MFA OTP mail payload for email: {}", getClass().getSimpleName(), mfaOtpPayload.getEmail());
        IPResponse ipResponse = ipService.getIpDetails(ipUtils.getIpv4());
        mfaOtpPayload.setOs(servletUtil.os());
        mfaOtpPayload.setBrowser(servletUtil.browser());
        mfaOtpPayload.setRemoteAddress(ipUtils.getIpv4());
        mfaOtpPayload.setTimestamp(DateUtil.templateFormattedDate(LocalDateTime.now()));
        mfaOtpPayload.setLocation(String.format("%s, %s", ipResponse.getCity(), ipResponse.getCountry()));
    }
}
