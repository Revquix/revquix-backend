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
package com.revquix.backend.auth.processor;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: PostLoginProcessor
 */

import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.BadRequestException;
import com.revquix.backend.application.utils.IpUtils;
import com.revquix.backend.auth.cache.UserAuthCache;
import com.revquix.backend.auth.dao.repository.UserAuthRepository;
import com.revquix.backend.auth.model.UserAuth;
import com.revquix.backend.auth.payload.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostLoginProcessor {

    private final UserAuthCache userAuthCache;
    private final IpUtils ipUtils;
    private final UserAuthRepository userAuthRepository;

    public void process(UserIdentity userIdentity) {
        log.info("{}::process -> Post login processing for user: {}", getClass().getSimpleName(), userIdentity.getUserId());
        UserAuth userAuth = userAuthCache.findById(userIdentity.getUserId())
                .orElseThrow(() -> new BadRequestException(ErrorData.USER_NOT_FOUND_BY_ID));
        userAuth.setLastLoginIp(ipUtils.getIpv4());
        UserAuth userAuthResponse = userAuthRepository.save(userAuth);
        log.info("{}::process -> Updated last login IP for user: {}", getClass().getSimpleName(), userAuthResponse.getUserId());
        userAuthCache.put(userAuthResponse);
        log.info("{}::process -> Post login processing completed for user: {}", getClass().getSimpleName(), userIdentity.getUserId());
    }
}
