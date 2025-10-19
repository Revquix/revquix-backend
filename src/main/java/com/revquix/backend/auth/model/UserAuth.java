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
  File: UserAuth
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revquix.backend.application.annotation.ModelId;
import com.revquix.backend.application.annotation.impl.ModelIdListener;
import com.revquix.backend.application.constants.ModelConstants;
import com.revquix.backend.application.utils.ModelPayload;
import com.revquix.backend.auth.enums.UserBadge;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(
        name = ModelConstants.USER_AUTH_TABLE,
        schema = ModelConstants.AUTH_SCHEMA,
        indexes = {
                @Index(
                        name = "idx_userauth_userId",
                        columnList = "userId"
                ),
                @Index(
                        name = "idx_userauth_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_userauth_username",
                        columnList = "username"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_userauth_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_userauth_username", columnNames = "username")
        }
)
@EntityListeners({ModelIdListener.class, AuditingEntityListener.class})
public class UserAuth extends ModelPayload<UserAuth> {

    @Id
    @ModelId(prefix = "UA", sequence = "auth.user_auth_id_seq", length = 6)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean isEmailVerified = false;

    @Builder.Default
    private boolean isEnabled = true;

    @Builder.Default
    private Boolean isAccountNonLocked = true;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    @Builder.Default
    private List<String> authProvider = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dateUpdated;

    private LocalDateTime lastPasswordChange;

    private String registerIp;
    private String lastLoginIp;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name =  ModelConstants.USER_ROLE_JOIN_TABLE,
            schema = ModelConstants.AUTH_SCHEMA,
            joinColumns = @JoinColumn(name = "user_auth_id", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "roleId")
    )
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserBadge userBadge = UserBadge.STANDARD;

    private LocalDateTime lastUsernameChange;

    @Builder.Default
    private boolean mfaEnabled = false;
}
