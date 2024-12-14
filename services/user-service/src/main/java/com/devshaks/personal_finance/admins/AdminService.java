package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.exceptions.AdminRegistrationException;
import com.devshaks.personal_finance.exceptions.UserNotFoundException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.*;
import com.devshaks.personal_finance.users.UserDTO;
import com.devshaks.personal_finance.users.UserMapper;
import com.devshaks.personal_finance.users.UserRepository;
import com.devshaks.personal_finance.utility.AgeVerification;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static com.devshaks.personal_finance.kafka.UserEvents.ADMIN_REGISTERED;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final String predefinedSuperAdminCode = "12345"; // HARD CODED FOR NOW
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CreateAuditEvent createKafkaAuditEvent;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AgeVerification ageVerification;

    private void validateAdminRegistrationRequest(@Valid AdminRegistrationRequest registrationRequest,
            String adminCode) {
        if (registrationRequest == null) {
            throw new AdminRegistrationException("Admin Registration Cannot be Null");
        }
        if (registrationRequest.dateOfBirth() == null) {
            throw new AdminRegistrationException("Date of Birth is required");
        }
        if (!predefinedSuperAdminCode.equals(adminCode)) {
            throw new AdminRegistrationException("Super Admin Code Not Matched");
        }
    }

    public AdminDTO registerAdmin(@Valid AdminRegistrationRequest adminRegistrationRequest) {
        try {
            validateAdminRegistrationRequest(adminRegistrationRequest, predefinedSuperAdminCode);
            LocalDate dateOfBirth = adminRegistrationRequest.dateOfBirth();
            if (!ageVerification.isUserAdult(dateOfBirth)) {
                throw new AdminRegistrationException("Admin Must be Over 18");
            }
            Admin admin = adminMapper.toAdminRegistration(adminRegistrationRequest);
            String generatedUsername = usernameGenerator.generateAdminUsername(admin.getDateOfBirth().getYear());
            admin.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(admin.getPassword());
            admin.setPassword(encodedPassword);

            if (admin.getPermissions() == null) {
                admin.setPermissions(EnumSet.noneOf(AdminPermissions.class));
            }
            Admin savedAdmin = adminRepository.save(admin);
            createKafkaAuditEvent.sendAuditEvent(ADMIN_REGISTERED, admin.getId(), "Admin Registered Successfully");
            return adminMapper.toAdminDTO(savedAdmin);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized Request to Create Admin", exception);
            }
            throw exception;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDTO)
                .toList();
    }

    public UserDTO getUserDetails(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> new UserNotFoundException("Cannot Find User: " + userId));
    }
}
