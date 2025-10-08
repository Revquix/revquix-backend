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
package com.revquix.backend.application.annotation.impl;

/*
  Developer: Rohit Parihar
  Project: ap-payment-service
  GitHub: github.com/rohit-zip
  File: CustomIdGeneratorListener
 */

import com.revquix.backend.application.annotation.ModelId;
import com.revquix.backend.application.exception.ErrorData;
import com.revquix.backend.application.exception.payload.InternalServerException;
import com.revquix.backend.application.utils.SequenceEntityContextBridge;
import jakarta.persistence.PrePersist;

import java.lang.reflect.Field;

public class ModelIdListener {

    @PrePersist
    public void generateCustomId(Object entity) {
        Class<?> clazz = entity.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ModelId.class)) {
                try {
                    field.setAccessible(true);
                    Object currentValue = field.get(entity);
                    if (currentValue != null) continue;

                    ModelId annotation = field.getAnnotation(ModelId.class);
                    String prefix = annotation.prefix();
                    String sequence = annotation.sequence();
                    int length = annotation.length();
                    if (sequence == null || sequence.trim().isEmpty()) {
                        throw new InternalServerException(ErrorData.SEQUENCE_NULL_OR_EMPTY);
                    }
                    String customId = generateSequenceId(prefix, sequence, length);
                    field.set(entity, customId);

                } catch (IllegalAccessException e) {
                    throw new InternalServerException(ErrorData.FAILED_TO_GENERATE_SEQUENCE, String.format("Failed to generate ID for field: %s", field.getName()));
                } catch (Exception e) {
                    throw new InternalServerException(ErrorData.ID_GENERATION_FAILED, String.format("ID Generation failed for field: %s", field.getName()));
                }
            }
        }
    }

    private synchronized String generateSequenceId(String prefix, String sequence, int length) {
        Long nextVal = SequenceEntityContextBridge.getSequenceService()
                .getNextSequenceValue(sequence);
        return String.format("%s%0" + length + "d", prefix, nextVal);
    }

}
