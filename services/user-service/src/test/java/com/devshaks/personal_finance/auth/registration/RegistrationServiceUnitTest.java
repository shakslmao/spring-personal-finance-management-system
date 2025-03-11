package com.devshaks.personal_finance.auth.registration;

import com.devshaks.personal_finance.auth.dto.UserRegistrationRequest;
import com.devshaks.personal_finance.auth.service.NotificationService;
import com.devshaks.personal_finance.auth.service.RegistrationService;
import com.devshaks.personal_finance.auth.service.TokenService;
import com.devshaks.personal_finance.exceptions.UserRegistrationException;
import com.devshaks.personal_finance.kafka.audit.AuditEventSender;
import com.devshaks.personal_finance.users.*;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgeVerification ageVerification;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditEventSender auditEventSender;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RegistrationService registrationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstname("test")
                .lastname("test")
                .email("test@test.com")
                .password("password")
                .roles(UserRoles.USER)
                .status(AccountStatus.ACTIVE_NON_AUTH)
                .dateOfBirth(LocalDate.of(2001,1,1))
                .build();
    }

    @Test
    void registerUser_ShouldThrowException_WhenRequestIsNull() {
        assertThrows(RuntimeException.class, () -> registrationService.registerUser(null));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExist() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest("test","test","test@emai.com","password",LocalDate.of(2000,1,1));
        when(userRepository.existsByEmail(userRegistrationRequest.email())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> registrationService.registerUser(userRegistrationRequest), "Email already exists");
    }

    @Test
    void registerUser_ShouldRegisterUser_WhenDateOfBirthIsNull() {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest("test","test","test@emai.com","password", null);
        assertThrows(RuntimeException.class, () -> registrationService.registerUser(userRegistrationRequest), "Date of Birth is required");
    }

    @Test
    void registerUser_ShouldThrowException_WhenUserIsUnderage() {
        UserRegistrationRequest request = new UserRegistrationRequest("test", "test", "test@email.com", "password", LocalDate.of(2010,1,1));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(ageVerification.isUserAdult(request.dateOfBirth())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> registrationService.registerUser(request), "User must be 18 years or older");
    }

    @Test
    void registerUser_ShouldCreateUser_WhenValidRequest() throws MessagingException {
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest("test","test","test@test.com","password",LocalDate.of(2001,1,1));
        UserDTO expectedRegistrationDTO = new UserDTO(1L,"test","generatedUsername2001","test@test.com", LocalDate.of(2001,1,1), UserRoles.USER, AccountStatus.ACTIVE_NON_AUTH);
        String activationToken = "activation-token";

        when(userRepository.existsByEmail(userRegistrationRequest.email())).thenReturn(false);
        when(ageVerification.isUserAdult(userRegistrationRequest.dateOfBirth())).thenReturn(true);
        when(userMapper.toUserRegistration(userRegistrationRequest)).thenReturn(testUser);
        when(usernameGenerator.generateUsername(anyInt())).thenReturn("generatedUsername2001");
        when(passwordEncoder.encode(userRegistrationRequest.password())).thenReturn("generatedPassword");
        when(userMapper.toUserDTO(testUser)).thenReturn(expectedRegistrationDTO);
        when(tokenService.generateAndSaveActivationToken(any(User.class))).thenReturn(activationToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO userDTO = registrationService.registerUser(userRegistrationRequest);

        assertNotNull(userDTO);
        assertEquals("test@test.com", userDTO.email());
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService).generateAndSaveActivationToken(any(User.class));
        verify(auditEventSender, times(1)).sendAuditEventFromUser(any(), any(), any());
        verify(notificationService).sendAccountActivationEmail(any(), any());
    }
}
