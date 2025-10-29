package com.revquix.backend.auth.processor;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: MfaOtpProcessor
 */

import com.revquix.backend.auth.model.MfaEntity;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaOtpProcessor {

    private final AuthenticationProperties authenticationProperties;

    public void process(MfaEntity mfaEntity) {
        log.info("{}::process -> Processing MFA OTP for MFA Entity: {}", getClass().getSimpleName(), mfaEntity.toJson());
        AuthenticationProperties.Mfa mfa = authenticationProperties.getMfa();
        if (mfa.isEnabled()) {
            log.info("{}::process -> MFA is enabled in properties. Proceeding with OTP processing for MFA Entity: {}", getClass().getSimpleName());

        } else {
            log.warn("{}::process -> MFA is disabled in properties. Skipping OTP processing for MFA Entity: {}", getClass().getSimpleName());
        }
    }
}
