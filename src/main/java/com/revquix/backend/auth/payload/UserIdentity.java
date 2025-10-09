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
package com.revquix.backend.auth.payload;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: UserIdentity
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revquix.backend.auth.enums.UserBadge;
import com.revquix.backend.auth.model.Role;
import com.revquix.backend.auth.model.UserAuth;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIdentity implements UserDetails {

    private String userId;
    private String email;
    private String username;
    private String password;
    private boolean isEnabled;
    private boolean isAccountNonLocked;
    private LocalDateTime lastPasswordChange;
    private boolean isEmailVerified;
    private String lastLoginIp;
    private UserBadge userBadge;
    private Collection<? extends GrantedAuthority> authorities;

    public UserIdentity(
            String userId,
            String email,
            String username,
            String password,
            boolean isEnabled,
            boolean isAccountNonLocked,
            LocalDateTime lastPasswordChange,
            boolean isEmailVerified,
            String lastLoginIp,
            UserBadge userBadge,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.isAccountNonLocked = isAccountNonLocked;
        this.lastPasswordChange = lastPasswordChange;
        this.isEmailVerified = isEmailVerified;
        this.lastLoginIp = lastLoginIp;
        this.userBadge = userBadge;
        this.authorities = authorities;
    }

    public static UserIdentity create(UserAuth userAuth) {
        List<Role> roles = userAuth.getRoles();
        List<SimpleGrantedAuthority> grantedAuthorities = parseRoles(roles).stream().map(SimpleGrantedAuthority::new).toList();
        return new UserIdentity(
                userAuth.getUserId(),
                userAuth.getEmail(),
                userAuth.getUsername(),
                userAuth.getPassword(),
                userAuth.isEnabled(),
                userAuth.getIsAccountNonLocked(),
                userAuth.getLastPasswordChange(),
                userAuth.isEmailVerified(),
                userAuth.getLastLoginIp(),
                userAuth.getUserBadge(),
                grantedAuthorities
        );
    }

    private static List<String> parseRoles(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return Collections.emptyList();
        List<String> parsedRoles = new ArrayList<>();
        roles.forEach(role -> {
            parsedRoles.add(role.getRole());
            List<String> internalRoles = role.getInternalRoles();
            if (internalRoles != null) parsedRoles.addAll(internalRoles);
        });
        return parsedRoles
                .stream()
                .distinct()
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
