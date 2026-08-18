package com.magaita.appointment.controller;


import com.magaita.appointment.dto.UpdatePasswordRequest;
import com.magaita.appointment.dto.UserDto;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Response<UserDto>> getMyUserDetails(){
        return ResponseEntity.ok(userService.getMyUserDetails());
    }

    @GetMapping("/by-id/{userId}")
    public ResponseEntity<Response<UserDto>> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDto>>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<?>> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest){
        return ResponseEntity.ok(userService.updatePassword(updatePasswordRequest));
    }

    @PutMapping("/profile-picture")
    public ResponseEntity<Response<?>> uploadProfilePicture(@RequestParam("file") MultipartFile file){
        return ResponseEntity.ok(userService.uploadProfilePicture(file));
    }
}
