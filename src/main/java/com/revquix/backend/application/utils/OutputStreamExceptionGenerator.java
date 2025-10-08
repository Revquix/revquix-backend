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
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.payload.ExceptionResponse;
import com.revquix.backend.application.payload.OutputStreamErrorPayload;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@UtilityClass
@Slf4j
public class OutputStreamExceptionGenerator {

    @SneakyThrows(value = {IOException.class})
    public static void generateExceptionResponse(ErrorData errorData, HttpStatus httpStatus, HttpServletResponse httpServletResponse) {
        ExceptionResponse exceptionResponse = ExceptionResponse
                .builder()
                .code(errorData.getCode())
                .message(errorData.getMessage())
                .breadcrumbId(MDC.get(ServiceConstants.BREADCRUMB_ID))
                .isTokenExpired(false)
                .build();
        OutputStreamUtil.getOutputStream(new OutputStreamErrorPayload(httpStatus, exceptionResponse, httpServletResponse));
        log.error("ExceptionResponseGenerator -> {}", exceptionResponse.toString());
    }
}
