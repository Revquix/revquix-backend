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
package com.revquix.backend.auth.authentication;

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.InternalServerException;
import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyProvider {

    private final AuthenticationProperties authenticationProperties;

    public PrivateKey getPrivateKey() {
        log.debug("{}::getPrivateKey", getClass().getSimpleName());
        try {
            KeyStore keyStore = KeyStore.getInstance(ServiceConstants.PKCS12);
            AuthenticationProperties.KeyData keyData = authenticationProperties.getKeyData();
            try (InputStream keyStoreInputStream = getClass().getResourceAsStream(keyData.getPassword())) {
                keyStore.load(keyStoreInputStream, keyData.getPassword().toCharArray());
            }
            return (PrivateKey) keyStore.getKey(keyData.getAlias(), keyData.getPassword().toCharArray());
        } catch (Exception exception) {
            throw new InternalServerException(
                    ErrorData.EXCEPTION_WHILE_FETCHING_PRIVATE_KEY,
                    exception.getCause()
            );
        }
    }

    public PublicKey getPublicKey() {
        log.debug("{}::getPublicKey", getClass().getSimpleName());
        try {
            KeyStore keyStore = KeyStore.getInstance(ServiceConstants.PKCS12);
            AuthenticationProperties.KeyData keyData = authenticationProperties.getKeyData();
            try (InputStream keyStoreInputStream = getClass().getResourceAsStream(keyData.getPath())) {
                keyStore.load(keyStoreInputStream, keyData.getPassword().toCharArray());
            }
            Certificate certificate = keyStore.getCertificate(keyData.getAlias());
            return certificate.getPublicKey();
        } catch (Exception exception) {
            throw new InternalServerException(
                    ErrorData.EXCEPTION_WHILE_FETCHING_PUBLIC_KEY,
                    exception.getCause()
            );
        }
    }
}
