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
  File: SendRegistrationOtpMail
 */

import com.revquix.backend.auth.model.OtpEntity;
import com.revquix.backend.notification.payload.RegistrationOtpPayload;
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
public class SendRegistrationOtpMail {

    private final MailProperties mailProperties;
    private final ZeptoMailService zeptoMailService;
    private final SpringTemplateEngine templateEngine;

    public void execute(RegistrationOtpPayload registrationOtpPayload) {
        log.info("{}::execute -> Sending registration OTP mail {}", getClass().getSimpleName(), registrationOtpPayload.toJson());
        MailProperties.Registration registration = mailProperties.getZeptoMail().getRegistration();
        ZeptoMailResponse zeptoMailResponse = zeptoMailService.send(
                registration.getPrefix(),
                registrationOtpPayload.getEmail(),
                registration.getSubject(),
                prepareContext(registrationOtpPayload)
        );
        log.info("{}::execute -> OTP mail sent successfully with response: {}", getClass().getSimpleName(), zeptoMailResponse.toJson());
    }

    private String prepareContext(RegistrationOtpPayload registrationOtpPayload) {
        log.info("SendRegistrationOtpMail::prepareContext -> Preparing email context for Registration OTP mail {}");
        MailProperties.Registration registration = mailProperties.getZeptoMail().getRegistration();
        Context context = new Context();
        context.setVariable(registration.getContext(), registrationOtpPayload);
        return templateEngine.process(registration.getTemplate(), context);
    }
}
