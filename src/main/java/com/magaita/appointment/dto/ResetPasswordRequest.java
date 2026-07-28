package com.magaita.appointment.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {

    //will be used to request the forgotten password
    private String email;

    //will be used to set the new password
    private String code;
    private String newPassword;
}
