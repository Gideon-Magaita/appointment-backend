package com.magaita.appointment.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.magaita.appointment.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDto {

    private Long id;

    @NotNull(message = "Doctor Id is required for booking an appointment")
    private Long doctorId;

    @NotNull(message = "Start time is required for the appointment")
    @Future(message = "Appointment must be scheduled for future time and date")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String meetingLink;

    private String purposeOfConsultation;

    private String initialSymptoms;

    private AppointmentStatus status;

    private DoctorDto doctor;

    private PatientDto patient;

}
