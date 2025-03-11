package com.devshaks.personal_finance.auth.token;

import com.devshaks.personal_finance.auth.service.TokenService;
import com.devshaks.personal_finance.token.Tokens;
import com.devshaks.personal_finance.token.TokensRepository;
import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class TokenServiceIntegrationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokensRepository tokensRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .firstname("Test")
                .lastname("User")
                .email("test@test.com")
                .password("password")
                .status(AccountStatus.ACTIVE_NON_AUTH)
                .dateOfBirth(LocalDate.of(2001, 1, 1))
                .build();
    }

    @Test
    void generateAndSaveActivationToken_ShouldSaveTokenInDatabase() {
        String token = tokenService.generateAndSaveActivationToken(testUser);

        assertNotNull(token);

        Optional<Tokens> savedToken = tokensRepository.findByToken(token);
        assertTrue(savedToken.isPresent(), "Token Should be saved in the database");
        assertEquals(testUser.getId(), savedToken.get().getUser().getId());
    }




}
