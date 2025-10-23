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
package com.revquix.backend.auth.model;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: MfaEntity
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.revquix.backend.application.constants.ModelConstants;
import com.revquix.backend.application.utils.MaskingSerializer;
import com.revquix.backend.application.utils.ModelPayload;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(
        name = ModelConstants.MFA,
        schema = ModelConstants.AUTH_SCHEMA
)
@EntityListeners(AuditingEntityListener.class)
public class MfaEntity extends ModelPayload<MfaEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String mfaId;

    @Column(nullable = false, updatable = false, length = 700)
    @JsonSerialize(using = MaskingSerializer.class)
    private String token;

    @Column(nullable = false, updatable = false, length = 10)
    private String otp;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiresIn;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Column(nullable = false, updatable = false)
    private String remoteAddress;

    @Column(nullable = false, updatable = false)
    private String os;

    @Column(nullable = false, updatable = false)
    private String browser;
}
