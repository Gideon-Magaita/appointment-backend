package com.magaita.appointment.service;


import com.magaita.appointment.dto.LoginRequest;
import com.magaita.appointment.dto.LoginResponse;
import com.magaita.appointment.dto.RegistrationRequest;
import com.magaita.appointment.dto.ResetPasswordRequest;
import com.magaita.appointment.res.Response;

public interface AuthService {

    Response<String>register(RegistrationRequest request);

    Response<LoginResponse> login(LoginRequest loginRequest);

    Response<?>forgetPassword(String email);

    Response<?>updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);

}
