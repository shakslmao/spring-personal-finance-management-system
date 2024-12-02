package com.devshaks.personal_finance.user_service;

import com.devshaks.personal_finance.user_service.user.users.*;
import com.devshaks.personal_finance.user_service.user.utility.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest request;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        request = new UserRegistrationRequest(
                "John",
                "john.doe@example.com",
                "securePassword",
                LocalDate.of(1990, 1, 1)
        );

        user = User.builder()
                .firstname("John")
                .email("john.doe@example.com")
                .password("hashedPassword")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .roles(UserRoles.USER)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        User mockUser = user.builder()
                .firstname("John")
                .email("john.doe@example.com")
                .password("securePassword")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .roles(UserRoles.USER)
                .build();

        Mockito.when(userMapper.toUserRegistration(request)).thenReturn(user);
        Mockito.when(userRepository.existsByEmail(request.email())).thenReturn(false);
        Mockito.when(usernameGenerator.generateUsername(1990)).thenReturn("123490");
        Mockito.when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

       // User registeredUser = userService.registerUser(request);

       // Assertions.assertNotNull(registeredUser, "Registered user should not be null");
       // Assertions.assertEquals("123490", registeredUser.getUsername(), "Generated username mismatch");
       // Assertions.assertEquals("hashedPassword", registeredUser.getPassword(), "Encoded password mismatch");

        Mockito.verify(userMapper).toUserRegistration(request);
        Mockito.verify(usernameGenerator).generateUsername(1990);
        Mockito.verify(passwordEncoder).encode(request.password());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }
}

