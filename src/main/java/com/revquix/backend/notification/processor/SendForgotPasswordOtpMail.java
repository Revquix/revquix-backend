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

import com.revquix.backend.notification.payload.ForgotPasswordOtpPayload;
import com.revquix.backend.notification.payload.ZeptoMailResponse;
import com.revquix.backend.notification.properties.MailProperties;
import com.revquix.backend.notification.service.ZeptoMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendForgotPasswordOtpMail {

    private final MailProperties mailProperties;
    private final ZeptoMailService zeptoMailService;
    private final SpringTemplateEngine templateEngine;

    public void execute(ForgotPasswordOtpPayload forgotPasswordOtpPayload) {
        log.info("{}::execute -> Sending Forgot Password OTP mail {}", getClass().getSimpleName(), forgotPasswordOtpPayload.toJson());
        MailProperties.ForgotPassword forgotPassword = mailProperties.getZeptoMail().getForgotPassword();
        ZeptoMailResponse zeptoMailResponse = zeptoMailService.send(
                forgotPassword.getPrefix(),
                forgotPasswordOtpPayload.getEmail(),
                forgotPassword.getSubject(),
                prepareContext(forgotPasswordOtpPayload),
                forgotPassword.getName()
        );
        log.info("{}::execute -> OTP mail sent successfully with response: {}", getClass().getSimpleName(), zeptoMailResponse.toJson());
    }

    private String prepareContext(ForgotPasswordOtpPayload forgotPasswordOtpPayload) {
        log.info("{}::prepareContext -> Preparing email context for Forgot Password OTP mail", getClass().getSimpleName());
        MailProperties.ForgotPassword forgotPassword = mailProperties.getZeptoMail().getForgotPassword();
        Context context = new Context();
        context.setVariable(forgotPassword.getContext(), forgotPasswordOtpPayload);
        return templateEngine.process(forgotPassword.getTemplate(), context);
    }
}
