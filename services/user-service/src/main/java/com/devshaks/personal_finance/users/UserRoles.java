package com.devshaks.personal_finance.users;

public enum UserRoles {
    USER,
    ADMIN,
    SUPER_ADMIN,
    NOT_ASSIGNED;

    public boolean isElevatedRole() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
