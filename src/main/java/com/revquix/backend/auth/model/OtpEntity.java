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
  File: OtpEntity
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revquix.backend.application.constants.ModelConstants;
import com.revquix.backend.application.utils.ModelPayload;
import com.revquix.backend.auth.enums.OtpFor;
import com.revquix.backend.auth.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
        name = ModelConstants.OTP_ENTITY_TABLE,
        schema = ModelConstants.AUTH_SCHEMA
)
@EntityListeners(AuditingEntityListener.class)
public class OtpEntity extends ModelPayload<OtpEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String otpId;

    @Column(nullable = false, unique = true)
    private String otp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpFor otpFor;

    @Column(nullable = false)
    private String email;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dateUpdated;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    private int timesSent = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpStatus otpStatus;

    @Column(nullable = false)
    private String userId;
}
