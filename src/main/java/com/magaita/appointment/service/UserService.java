package com.magaita.appointment.service;

import com.magaita.appointment.dto.UpdatePasswordRequest;
import com.magaita.appointment.dto.UserDto;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.res.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User getCurrentUser();

    Response<UserDto> getMyUserDetails();

    Response<UserDto> getUserById(Long userId);

    Response<List<UserDto>> getAllUsers();

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    Response<?> uploadProfilePicture(MultipartFile file);

}
