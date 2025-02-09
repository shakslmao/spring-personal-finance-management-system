package com.devshaks.personal_finance.token;

import java.time.LocalDateTime;

import com.devshaks.personal_finance.users.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Tokens {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(unique = true)
    private String refreshToken;

    @Builder.Default
    private boolean isUsed = false;

    @Builder.Default
    private boolean isRevoked = false;

    @Builder.Default
    private boolean expired = false;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
