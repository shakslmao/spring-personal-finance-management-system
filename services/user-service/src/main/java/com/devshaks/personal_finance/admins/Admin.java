package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String adminCode;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRoles roles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<AdminPermissions> permissions = EnumSet.noneOf(AdminPermissions.class);

    @LastModifiedDate
    private LocalDateTime lastAccessedAt;

    public Set<AdminPermissions> getPermissions() {
        return Collections.unmodifiableSet(permissions != null ? permissions : EnumSet.noneOf(AdminPermissions.class));
    }

    public void addPermission(AdminPermissions permission) {
        if (this.permissions == null) {
            this.permissions = EnumSet.noneOf(AdminPermissions.class);
        }
        this.permissions.add(permission);
    }

    public void removePermission(AdminPermissions permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }
}

