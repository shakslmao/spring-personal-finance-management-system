package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.users.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Admin extends User {
    private String adminCode;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<AdminPermissions> permissions = EnumSet.noneOf(AdminPermissions.class);

    @LastModifiedDate
    private LocalDateTime lastAccessedAt;

    public Set<AdminPermissions> getPermissions() {
        return Collections.unmodifiableSet(permissions != null ? permissions : EnumSet.noneOf(AdminPermissions.class));
    }

    public void addPermission(AdminPermissions permissions) {
        if (this.permissions == null) {
            this.permissions = EnumSet.noneOf(AdminPermissions.class);
        }
        this.permissions.add(permissions);
    }

    public void removePermission(AdminPermissions permissions) {
        if (this.permissions != null) {
            this.permissions.remove(permissions);
        }
    }
}
