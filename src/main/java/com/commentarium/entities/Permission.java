package com.commentarium.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    MANAGER_READ("moderator:read"),
    MANAGER_UPDATE("moderator:update"),
    MANAGER_CREATE("moderator:create"),
    MANAGER_DELETE("moderator:delete")

    ;

    @Getter
    private final String permission;
}