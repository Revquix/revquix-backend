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
package com.revquix.backend.application.utils;

import com.revquix.backend.application.constants.ServiceConstants;
import com.revquix.backend.application.exception.BaseException;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.payload.ExceptionResponse;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.LocalDateTime;

@UtilityClass
public class ErrorResponseGeneratorUtil {

    public ExceptionResponse generate(BaseException exception) {
        String breadcrumbId = MDC.get(ServiceConstants.BREADCRUMB_ID);
        String localizedMessage = exception.getCause() != null ? exception.getCause().getLocalizedMessage() : exception.getMessage();
        return ExceptionResponse
                .builder()
                .message(exception.getMessage())
                .breadcrumbId(breadcrumbId)
                .code(exception.getCode())
                .httpStatus(exception.getHttpStatus().getReasonPhrase())
                .localizedMessage(localizedMessage)
                .errorType(exception.getCode().contains("DE-") ? ServiceConstants.DATA_ERROR : ServiceConstants.INTERNAL_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ExceptionResponse generate(AuthorizationDeniedException exception) {
        String breadcrumbId = MDC.get(ServiceConstants.BREADCRUMB_ID);
        return ExceptionResponse
                .builder()
                .message(ErrorData.ACCESS_DENIED_ERROR_CODE.getMessage())
                .breadcrumbId(breadcrumbId)
                .code(ErrorData.ACCESS_DENIED_ERROR_CODE.getCode())
                .httpStatus(HttpStatus.FORBIDDEN.getReasonPhrase())
                .localizedMessage(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
