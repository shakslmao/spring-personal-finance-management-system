package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Entity
public class Admin extends User {
    @Column(nullable = false, unique = true)
    private String adminCode;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AdminPermissions> permissions = EnumSet.noneOf(AdminPermissions.class);

    @LastModifiedDate
    private LocalDateTime lastAccessedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;

    public Set<AdminPermissions> getPermissions() { return Collections.unmodifiableSet(permissions); }
    public void addPermission(AdminPermissions permissions) { this.permissions.add(permissions); }
    public void removePermission(AdminPermissions permissions) { this.permissions.remove(permissions); }
}
