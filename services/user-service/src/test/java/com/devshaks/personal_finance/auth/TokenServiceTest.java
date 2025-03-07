package com.devshaks.personal_finance.auth;


import com.devshaks.personal_finance.auth.dto.RequestNewTokenResponse;
import com.devshaks.personal_finance.auth.service.NotificationService;
import com.devshaks.personal_finance.auth.service.TokenService;
import com.devshaks.personal_finance.token.Tokens;
import com.devshaks.personal_finance.token.TokensRepository;
import com.devshaks.personal_finance.users.AccountStatus;
import com.devshaks.personal_finance.users.User;
import com.devshaks.personal_finance.users.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private TokensRepository tokensRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstname("Test")
                .lastname("User")
                .email("test@test.com")
                .password("password")
                .status(AccountStatus.ACTIVE_NON_AUTH)
                .dateOfBirth(LocalDate.of(2001,1,1))
                .build();
    }

    @Test
    void generateAndSaveActivationToken_ShouldReturnValidToken() {
        // Arrange
        when(tokensRepository.save(any(Tokens.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String token = tokenService.generateAndSaveActivationToken(testUser);

        // Assert
        assertNotNull(token, "Token should not be null");

        // Verify repository interaction
        ArgumentCaptor<Tokens> tokenCaptor = ArgumentCaptor.forClass(Tokens.class);
        verify(tokensRepository).save(tokenCaptor.capture());

        Tokens savedToken = tokenCaptor.getValue();
        assertEquals(testUser, savedToken.getUser(), "Token should be associated with correct user");
        assertEquals(token, savedToken.getToken(), "Generated token should match");

        // Verify token properties
        assertNotNull(savedToken.getCreatedAt(), "Created date should not be null");
        assertNotNull(savedToken.getExpiresAt(), "Expiration date should not be null");
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()),
                "Expiration date should be after creation date");
    }

    @Test
    void generateActivationToken_ShouldReturnValidNumericToken() {
        int length = 6;

        String activationToken = tokenService.generateActivationCode(length);

        assertNotNull(activationToken, "Token should not be null");
        assertEquals(length, activationToken.length(), "Token Length Should Match");
        assertTrue(activationToken.matches("\\d+"), "Activation Code Should Only Contain Digits");
    }

    @Test
    void requestNewActivationToken_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("emaildoesntexist@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> tokenService.requestNewActivationToken("emaildoesntexist@email.com"));
        verify(userRepository, times(1)).findByEmail("emaildoesntexist@email.com");
    }

    @Test
    void requestNewActivationToken_ShouldReturnMessage_WhenUserAlreadyActivated() throws MessagingException {
        // Arrange
        testUser.setStatus(AccountStatus.ACTIVE_AUTHENTICATED);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // Act
        RequestNewTokenResponse response = tokenService.requestNewActivationToken("test@test.com");

        // Assert
        assertNotNull(response);
        assertEquals("Account is already activated.", response.message());
        verify(tokensRepository, never()).deleteAllByUserId(anyLong());
        verify(tokensRepository, never()).findAllValidTokensByUser(anyLong());
    }

    @Test
    void requestNewActivationToken_ShouldRevokeAndDeleteExistingTokens() throws MessagingException {
        // Arrange
        Tokens oldTokens = Tokens.builder()
                .id(1L)
                .token("token12312312431241")
                .refreshToken("12312312312")
                .isUsed(true)
                .isRevoked(false)
                .expired(false)
                .user(testUser)
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(tokensRepository.findAllValidTokensByUser(testUser.getId())).thenReturn(List.of(oldTokens));

        // Act
        tokenService.requestNewActivationToken("test@test.com");

        // Assert
        assertTrue(oldTokens.isRevoked(), "Tokens should be revoked");
        verify(tokensRepository, times(1)).deleteAllByUserId(testUser.getId());

    }

    @Test
    void requestNewActivationToken_ShouldGenerateAndSaveToken() throws MessagingException {
        // ARRANGE
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(tokensRepository.findAllValidTokensByUser(testUser.getId())).thenReturn(List.of()); // User should have an empty list of tokens.

        // Spy
        TokenService spyTokenService = spy(tokenService);
        doReturn("12314455Token").when(spyTokenService).generateAndSaveActivationToken(testUser);

        // Act
        RequestNewTokenResponse response = spyTokenService.requestNewActivationToken("test@test.com");

        // Assert
        assertNotNull(response);
        assertEquals("A new activation token has been sent to your email.", response.message());
        verify(spyTokenService, times(1)).generateAndSaveActivationToken(testUser);
    }

    @Test
    void verifyAndActivateAccount_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(tokensRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> tokenService.verifyAndActivateAccount("invalid-token"));
        assertEquals("Invalid Token", exception.getMessage(), "Exception Message Should Match");
        verify(tokensRepository, times(1)).findByToken(anyString());
        verifyNoMoreInteractions(tokensRepository, userRepository, notificationService);
    }

    @Test // A new token is generated, a notification is sent, an exception is throws
    void verifyAndActivateAccount_ShouldGenerateTokenAfterExpiry() throws MessagingException {
        // Arrange
        Tokens expiredToken = Tokens.builder()
                .id(1L)
                .token("token12312312431241")
                .refreshToken("12312312312")
                .isUsed(false)
                .isRevoked(false)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .expired(true)
                .user(testUser)
                .build();

        TokenService spyTokenService = spy(tokenService);

        // mock repo call
        when(tokensRepository.findByToken(anyString())).thenReturn(Optional.of(expiredToken));
        lenient().when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // mock email sending
        doNothing().when(notificationService).sendAccountActivationEmail(any(User.class), anyString());

        // mock token generation
        doReturn("new-token").when(spyTokenService).generateAndSaveActivationToken(testUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> spyTokenService.verifyAndActivateAccount("expired-token") // <-- Call on spyTokenService
        );        assertEquals("Activation Token has Expired, a New Token Has Been Sent To Your Email", exception.getMessage(), "Exception Message Should Match");

        // Verify token generation and email sending
        verify(spyTokenService, times(1)).generateAndSaveActivationToken(testUser);
        verify(notificationService, times(1)).sendAccountActivationEmail(any(User.class), eq("new-token"));
    }









}
