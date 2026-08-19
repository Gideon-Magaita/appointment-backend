package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.ConsultationDto;
import com.magaita.appointment.entity.*;
import com.magaita.appointment.enums.AppointmentStatus;
import com.magaita.appointment.exceptions.BadRequestException;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.AppointmentRepo;
import com.magaita.appointment.repository.ConsultationRepo;
import com.magaita.appointment.repository.DoctorRepo;
import com.magaita.appointment.repository.PatientRepo;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.ConsultationService;
import com.magaita.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {

    private final UserService userService;
    private final ConsultationRepo consultationRepo;
    private final ModelMapper modelMapper;
    private final AppointmentRepo appointmentRepo;
    private final PatientRepo patientRepo;

    @Override
    public Response<ConsultationDto> createConsultation(ConsultationDto consultationDTO) {
        User user = userService.getCurrentUser();
        Long appointmentId = consultationDTO.getAppointmentId();

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));

        // Security Check 1: Must be the doctor linked to the appointment
        if (!appointment.getDoctor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to create notes for this consultation.");
        }
        // Complete the appointment
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(appointment);

        // Check 3: Ensure a consultation doesn't already exist for this appointment
        if (consultationRepo.findByAppointmentId(appointmentId).isPresent()) {
            throw new BadRequestException("Consultation notes already exist for this appointment.");
        }

        Consultation consultation = Consultation.builder()
                .consultationDate(LocalDateTime.now())
                .subjectiveNotes(consultationDTO.getSubjectiveNotes())
                .objectiveFindings(consultationDTO.getObjectiveFindings())
                .assessment(consultationDTO.getAssessment())
                .plan(consultationDTO.getPlan())
                .appointment(appointment)
                .build();

        consultationRepo.save(consultation);

        return Response.<ConsultationDto>builder()
                .statusCode(200)
                .message("Consultation notes saved successfully.")
                .build();


    }

    @Override
    public Response<ConsultationDto> getConsultationByAppointmentId(Long appointmentId) {
        Consultation consultation = consultationRepo.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new NotFoundException("Consultation notes not found for appointment ID: " + appointmentId));

        return Response.<ConsultationDto>builder()
                .statusCode(200)
                .message("Consultation notes retrieved successfully.")
                .data(modelMapper.map(consultation, ConsultationDto.class))
                .build();
    }

    @Override
    public Response<List<ConsultationDto>> getConsultationHistoryForPatient(Long patientId) {
        User user = userService.getCurrentUser();

        // 1. If patientId is null, retrieve the ID of the current authenticated patient.
        if (patientId == null) {
            Patient currentPatient = patientRepo.findByUser(user)
                    .orElseThrow(() -> new BadRequestException("Patient profile not found for the current user"));
            patientId = currentPatient.getId();
        }

        // Find the patient to ensure they exist (or to perform future security checks)
        patientRepo.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found "));

        // Use the repository method to fetch all consultations linked via appointments
        List<Consultation> history = consultationRepo.findByAppointmentPatientIdOrderByConsultationDateDesc(patientId);

        if (history.isEmpty()) {
            return Response.<List<ConsultationDto>>builder()
                    .statusCode(200)
                    .message("No consultation history found for this patient.")
                    .data(List.of())
                    .build();
        }

        List<ConsultationDto> historyDTOs = history.stream()
                .map(consultation -> modelMapper.map(consultation, ConsultationDto.class))
                .toList();

        return Response.<List<ConsultationDto>>builder()
                .statusCode(200)
                .message("Consultation history retrieved successfully.")
                .data(historyDTOs)
                .build();
    }
}
