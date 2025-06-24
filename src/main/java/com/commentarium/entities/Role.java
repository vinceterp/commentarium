package com.commentarium.entities;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.commentarium.entities.Permission.MANAGER_CREATE;
import static com.commentarium.entities.Permission.MANAGER_DELETE;
import static com.commentarium.entities.Permission.MANAGER_READ;
import static com.commentarium.entities.Permission.MANAGER_UPDATE;
import static com.commentarium.entities.Permission.ADMIN_CREATE;
import static com.commentarium.entities.Permission.ADMIN_DELETE;
import static com.commentarium.entities.Permission.ADMIN_READ;
import static com.commentarium.entities.Permission.ADMIN_UPDATE;

@RequiredArgsConstructor
public enum Role {
        USER(Collections.emptySet()),
        MODERATOR(
                        Set.of(
                                        MANAGER_READ,
                                        MANAGER_UPDATE,
                                        MANAGER_DELETE,
                                        MANAGER_CREATE)),
        ADMIN(
                        Set.of(
                                        ADMIN_READ,
                                        ADMIN_UPDATE,
                                        ADMIN_DELETE,
                                        ADMIN_CREATE,
                                        MANAGER_READ,
                                        MANAGER_UPDATE,
                                        MANAGER_DELETE,
                                        MANAGER_CREATE));

        @Getter
        private final Set<Permission> permissions;

        public List<SimpleGrantedAuthority> getAuthorities() {
                var authorities = getPermissions()
                                .stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                                .collect(Collectors.toList());
                authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
                return authorities;
        }
}
