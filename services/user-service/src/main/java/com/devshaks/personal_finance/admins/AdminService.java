package com.devshaks.personal_finance.admins;

import com.devshaks.personal_finance.exceptions.AdminRegistrationException;
import com.devshaks.personal_finance.handlers.UnauthorizedException;
import com.devshaks.personal_finance.kafka.AuditEventProducer;
import com.devshaks.personal_finance.kafka.AuditEvents;
import com.devshaks.personal_finance.kafka.EventType;
import com.devshaks.personal_finance.kafka.ServiceNames;
import com.devshaks.personal_finance.utility.UsernameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final String predefinedSuperAdminCode = "12345"; // HARD CODED FOR NOW
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventProducer auditEventProducer;

    private void validateAdminRegistrationRequest(@Valid AdminRegistrationRequest registrationRequest, String adminCode) {
        if (registrationRequest == null) { throw new AdminRegistrationException("Admin Registration Cannot be Null"); }
        if (registrationRequest.dateOfBirth() == null) { throw new AdminRegistrationException("Date of Birth is required"); }
        if (!predefinedSuperAdminCode.equals(adminCode)) { throw new AdminRegistrationException("Super Admin Code Not Matched"); }
    }

    public AdminDTO registerAdmin(@Valid AdminRegistrationRequest adminRegistrationRequest) {
        try {
            validateAdminRegistrationRequest(adminRegistrationRequest, predefinedSuperAdminCode);
            adminRepository.findByEmail(adminRegistrationRequest.email()).orElseThrow(() -> new AdminRegistrationException("Email Already Exists"));
            LocalDate dateOfBirth = adminRegistrationRequest.dateOfBirth();
            if (!isUserAdult(dateOfBirth)) { throw new AdminRegistrationException("Admin Must be Over 18"); }
            Admin admin = adminMapper.toAdminRegistration(adminRegistrationRequest);
            String generatedUsername = usernameGenerator.generateAdminUsername(admin.getDateOfBirth().getYear());
            admin.setUsername(generatedUsername);
            String encodedPassword = passwordEncoder.encode(admin.getPassword());
            admin.setPassword(encodedPassword);
            if (admin.getPermissions() == null) { admin.setPermissions(EnumSet.noneOf(AdminPermissions.class)); }
            Admin savedAdmin = adminRepository.save(admin);

            try {
                auditEventProducer.sendAuditEvent(new AuditEvents(
                        EventType.ADMIN_REGISTERED,
                        ServiceNames.USER_SERVICE,
                        savedAdmin.getId(),
                        "Admin Registered Successfully",
                        LocalDateTime.now().toString()
                ));
            } catch (KafkaException e) {
                throw new AdminRegistrationException("Error Sending Event to Audit Service");
            }

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

    private boolean isUserAdult(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(dateOfBirth, today);
        return age.getYears() >= 18;
    }
}
