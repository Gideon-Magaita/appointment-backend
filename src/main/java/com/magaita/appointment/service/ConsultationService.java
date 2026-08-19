package com.magaita.appointment.service;

import com.magaita.appointment.dto.ConsultationDto;
import com.magaita.appointment.res.Response;

import java.util.List;

public interface ConsultationService {

    Response<ConsultationDto> createConsultation(ConsultationDto consultationDTO);

    Response<ConsultationDto>getConsultationByAppointmentId(Long appointmentId);

    Response<List<ConsultationDto>>getConsultationHistoryForPatient(Long patientId);
}
