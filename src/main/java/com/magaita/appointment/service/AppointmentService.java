package com.magaita.appointment.service;

import com.magaita.appointment.dto.AppointmentDto;
import com.magaita.appointment.res.Response;

import java.util.List;

public interface AppointmentService {
    Response<AppointmentDto> bookAppointment(AppointmentDto appointmentDto);

    Response<List<AppointmentDto>> getMyAppointments();

    Response<?> cancelAppointment(Long appointmentId);

    Response<?> completeAppointment(Long appointmentId);
}
