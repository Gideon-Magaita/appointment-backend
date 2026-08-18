package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.NotificationDto;
import com.magaita.appointment.dto.UpdatePasswordRequest;
import com.magaita.appointment.dto.UserDto;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.exceptions.BadRequestException;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.UserRepository;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.NotificationService;
import com.magaita.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private final String uploadDir = "uploads/profile-pictures/"; //backend location for saving images
    //private final String uploadDir = "D:/AngularSpringBootTutorials/AppointmentScheduling/appointment-angular-frontend/public/products/"; //frontend location for saving images

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null){
            throw new NotFoundException("User is not authenticated!");
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("User not found!"));
    }

    @Override
    public Response<UserDto> getMyUserDetails() {
        User user = getCurrentUser();

        UserDto userDto = modelMapper.map(user,UserDto.class);
        return Response.<UserDto>builder()
                .statusCode(200)
                .message("User details retrieved successfully.")
                .data(userDto)
                .build();
    }

    @Override
    public Response<UserDto> getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        UserDto userDTO = modelMapper.map(user, UserDto.class);

        return Response.<UserDto>builder()
                .statusCode(200)
                .message("User details retrieved successfully.")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<List<UserDto>> getAllUsers() {
        List<UserDto> userDTOS = userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();

        return Response.<List<UserDto>>builder()
                .statusCode(200)
                .message("All users retrieved successfully.")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();


        if (oldPassword == null || newPassword == null) {
            throw new BadRequestException("Old and New Password Required");
        }

        // Validate the old password.
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old Password not Correct");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send password change confirmation email.
        NotificationDto notificationDTO = NotificationDto.builder()
                .recipient(user.getEmail())
                .subject("Your Password Was Successfully Changed")
                .templateName("password-change")
                .templateVariables(Map.of(
                        "name", user.getName()
                ))
                .build();
        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(200)
                .message("Password Changed Successfully")
                .build();

    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {
        User user = getCurrentUser();

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Path oldFile = Paths.get(user.getProfilePictureUrl());
                if (Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            }

            // Generate a unique file name to avoid conflicts
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")){
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(file.getInputStream(), filePath);
            String fileUrl = uploadDir + newFileName;

            //String fileUrl = "/profile-picture/" + newFileName;

            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile picture uploaded successfully.")
                    .data(fileUrl)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
