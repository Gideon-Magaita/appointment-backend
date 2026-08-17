package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.*;
import com.magaita.appointment.entity.Doctor;
import com.magaita.appointment.entity.Patient;
import com.magaita.appointment.entity.Role;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.exceptions.BadRequestException;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.DoctorRepo;
import com.magaita.appointment.repository.PatientRepo;
import com.magaita.appointment.repository.RoleRepository;
import com.magaita.appointment.repository.UserRepository;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.security.JwtService;
import com.magaita.appointment.service.AuthService;
import com.magaita.appointment.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Value("${login.link}")
    private String loginLink;

    @Override
    public Response<String> register(RegistrationRequest request) {
        /// 1. Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("User with email already exists");
        }

        // Determine the roles to assign. Default to PATIENT if none are provided.
        List<String> requestedRoleNames = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? request.getRoles().stream().map(String::toUpperCase).toList()
                : List.of("PATIENT");

        boolean isDoctor = requestedRoleNames.contains("DOCTOR");

        if (isDoctor && (request.getLisenceNumber() == null || request.getLisenceNumber().isBlank())) {
            throw new BadRequestException("License number required to register a doctor.");
        }

        /// 2. Load and validate roles from the database
        List<Role> roles = requestedRoleNames.stream()
                .map(roleRepository::findByName)
                .flatMap(Optional::stream)
                .toList();

        if (roles.isEmpty()) {
            throw new NotFoundException("Registration failed: Requested roles were not found in the database.");
        }

        /// 3. Create and save new user entity
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .roles(roles)
                .build();

        User savedUser = userRepository.save(newUser);

        log.info("New user registered: {} with {} roles.", savedUser.getEmail(), roles.size());

        /// 4. Process Profile Creation
        for (Role role : roles) {
            String roleName = role.getName();

            switch (roleName) {
                case "PATIENT":
                    createPatientProfile(savedUser);
                    log.info("Patient profile created: {}", savedUser.getEmail());
                    break;

                case "DOCTOR":
                    createDoctorProfile(request, savedUser);
                    log.info("Doctor profile created: {}", savedUser.getEmail());
                    break;

                case "ADMIN":
                    log.info("Admin role assigned to user: {}", savedUser.getEmail());
                    break;

                default:
                    log.warn("Assigned role '{}' has no corresponding profile creation logic.", roleName);
                    break;
            }
        }


        /// 5. Send welcome email out
        sendRegistrationEmail(request, savedUser);

        // 6. Return success response
        return Response.<String>builder()
                .statusCode(200)
                .message("Registration successful. A welcome email has been sent to you.")
                .data(savedUser.getEmail())
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {

        return null;
    }

    @Override
    public Response<?> forgetPassword(String email) {
        return null;
    }

    @Override
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        return null;
    }















    //Registration useful functions
    private void createPatientProfile(User user) {
        Patient patient = Patient.builder()
                .user(user)
                .build();

        patientRepo.save(patient);
        log.info("Patient profile created");
    }

    private void createDoctorProfile(RegistrationRequest request, User user) {
        Doctor doctor = Doctor.builder()
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLisenceNumber())
                .user(user)
                .build();

        doctorRepo.save(doctor);
    }

    private void sendRegistrationEmail(RegistrationRequest request, User user) {

        log.info("Trying to send Email Out");

        NotificationDto welcomeEmail = NotificationDto.builder()
                .recipient(user.getEmail())
                .subject("Welcome to Doctor Appointment System!")
                .templateName("welcome")
                .message("Thank you for registering Your account is ready.")
                .templateVariables(Map.of(
                        "name", request.getName(),
                        "loginLink", loginLink
                ))
                .build();

        notificationService.sendEmail(welcomeEmail, user);
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(5);
    }
}
