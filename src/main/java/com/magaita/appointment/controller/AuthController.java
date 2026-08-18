package com.magaita.appointment.controller;


import com.magaita.appointment.dto.LoginRequest;
import com.magaita.appointment.dto.LoginResponse;
import com.magaita.appointment.dto.RegistrationRequest;
import com.magaita.appointment.dto.ResetPasswordRequest;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<String>>register(@RequestBody @Valid RegistrationRequest request){
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>>login(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Response<?>>forgetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return ResponseEntity.ok(authService.forgetPassword(resetPasswordRequest.getEmail()));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return ResponseEntity.ok(authService.updatePasswordViaResetCode(resetPasswordRequest));
    }

}
