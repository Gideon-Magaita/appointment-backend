package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.AppointmentDto;
import com.magaita.appointment.dto.NotificationDto;
import com.magaita.appointment.entity.Appointment;
import com.magaita.appointment.entity.Doctor;
import com.magaita.appointment.entity.Patient;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.enums.AppointmentStatus;
import com.magaita.appointment.exceptions.BadRequestException;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.AppointmentRepo;
import com.magaita.appointment.repository.DoctorRepo;
import com.magaita.appointment.repository.PatientRepo;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.AppointmentService;
import com.magaita.appointment.service.NotificationService;
import com.magaita.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final UserService userService;
    private final AppointmentRepo appointmentRepo;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' hh:mm a");

    @Override
    public Response<AppointmentDto> bookAppointment(AppointmentDto appointmentDto) {

        User currentUser = userService.getCurrentUser();

        // 1. Get the patient initiating the booking
        Patient patient = patientRepo.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("Patient profile required for booking."));

        // 2. Get the target doctor
        Doctor doctor = doctorRepo.findById(appointmentDto.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found."));

        // --- START: VALIDATION LOGIC ---
        // Define the proposed time slot and the end time
        LocalDateTime startTime = appointmentDto.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(60); // Assuming 60-min slot

        // 3. Basic validation: booking must be at least 1 hour in advance
        if (startTime.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Appointments must be booked at least 1 hour in advance.");
        }

        //This code snippet logic used to enforce a mandatory one-hour break (or buffer) for the doctor before a new appointment.
        LocalDateTime checkStart = startTime.minusMinutes(60);

        // We only need to check for existing appointments whose END TIME overlaps with
        // the proposed start time, OR whose START TIME overlaps with the proposed end time.

        List<Appointment> conflicts = appointmentRepo.findConflictingAppointments(
                doctor.getId(),
                checkStart, // Check for conflicts from 1 hour before the proposed start
                endTime
        );

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Doctor is not available at the requested time. Please check their schedule.");
        }

        // 4a. Generate a unique, random string for the room name.
        //    (Your existing code is good for this)
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String uniqueRoomName = "dat-" + uuid.substring(0, 10);

        // 4b. Use the public Jitsi Meet domain with your unique room name
        String meetingLink = "https://meet.jit.si/" + uniqueRoomName;

        log.info("Generated Jitsi meeting link: {}", meetingLink);


        // 5. Build and Save Appointment
        Appointment appointment = Appointment.builder()
                .startTime(appointmentDto.getStartTime())
                .endTime(appointmentDto.getStartTime().plusMinutes(60)) // Assuming 60-min slot
                .meetingLink(meetingLink)
                .initialSymptoms(appointmentDto.getInitialSymptoms())
                .purposeOfConsultation(appointmentDto.getPurposeOfConsultation())
                .status(AppointmentStatus.SCHEDULED)
                .doctor(doctor)
                .patient(patient)
                .build();

        Appointment savedAppointment = appointmentRepo.save(appointment);

        sendAppointmentConfirmation(savedAppointment);

        return Response.<AppointmentDto>builder()
                .statusCode(200)
                .message("Appointment booked successfully.")
                .build();
    }

    @Override
    public Response<List<AppointmentDto>> getMyAppointments() {
        User user = userService.getCurrentUser();
        Long userId = user.getId();
        Optional<Appointment> appointments;


        // Check for "DOCTOR" role
        boolean isDoctor = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("DOCTOR"));

        if (isDoctor) {
            // 1. Check for Doctor profile existence (required to throw the correct exception)
            doctorRepo.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Doctor profile not found."));

            // 2. Efficiently fetch appointments of the Doctor
            appointments = appointmentRepo.findByDoctor_User_IdOrderByIdDesc(userId);
        } else {

            // 1. Check for Patient profile existence
            patientRepo.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Patient profile not found."));

            // 2. Efficiently fetch appointments using the User ID to navigate Patient relationship
            appointments = appointmentRepo.findByPatient_User_IdOrderByIdDesc(userId);
        }

        // Convert the list of entities to DTOs in a single step
        List<AppointmentDto> appointmentDTOList = appointments.stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentDto.class))
                .toList();

        return Response.<List<AppointmentDto>>builder()
                .statusCode(200)
                .message("Appointments retrieved successfully.")
                .data(appointmentDTOList)
                .build();
    }

    @Override
    public Response<?> cancelAppointment(Long appointmentId) {
        User user = userService.getCurrentUser();

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));

        // Add security check: only the patient or doctor involved can cancel
        boolean isOwner = appointment.getPatient().getUser().getId().equals(user.getId()) ||
                appointment.getDoctor().getUser().getId().equals(user.getId());

        if (!isOwner) {
            throw new BadRequestException("You do not have permission to cancel this appointment.");
        }


        // Update status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepo.save(appointment);

        // NOTE: Notification should be sent to the other party (patient/doctor)
        sendAppointmentCancellation(savedAppointment, user);

        return Response.<AppointmentDto>builder()
                .statusCode(200)
                .message("Appointment cancelled successfully.")
                .build();
    }

    @Override
    public Response<?> completeAppointment(Long appointmentId) {
        // Get the current user (must be the Doctor)
        User currentUser = userService.getCurrentUser();

        // 1. Fetch the appointment
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with ID: " + appointmentId));

        // Security Check 1: Ensure the current user is the Doctor assigned to this appointment
        if (!appointment.getDoctor().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Only the assigned doctor can mark this appointment as complete.");
        }

        // 2. Update status and end time
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setEndTime(LocalDateTime.now());

        appointmentRepo.save(appointment);

        return Response.builder()
                .statusCode(200)
                .message("Appointment successfully marked as completed. You may now proceed to create the consultation notes.")
                .build();
    }



    private void sendAppointmentConfirmation(Appointment appointment) {

        // --- 1. Prepare Patient Notification ---
        User patientUser = appointment.getPatient().getUser();
        String formattedTime = appointment.getStartTime().format(FORMATTER);


        Map<String, Object> patientVars = new HashMap<>();

        patientVars.put("patientName", patientUser.getName());
        patientVars.put("doctorName", appointment.getDoctor().getUser().getName());
        patientVars.put("appointmentTime", formattedTime);
        patientVars.put("isVirtual", true);
        patientVars.put("meetingLink", appointment.getMeetingLink());
        patientVars.put("purposeOfConsultation", appointment.getPurposeOfConsultation());

        NotificationDto patientNotification = NotificationDto.builder()
                .recipient(patientUser.getEmail())
                .subject("DAS: Your Appointment is Confirmed")
                .templateName("patient-appointment")
                .templateVariables(patientVars)
                .build();

        // Dispatch patient email using the low-level service
        notificationService.sendEmail(patientNotification, patientUser);
        log.info("Dispatched confirmation email for patient: {}", patientUser.getEmail());


        // --- 2. Prepare Doctor Notification ---
        User doctorUser = appointment.getDoctor().getUser();

        Map<String, Object> doctorVars = new HashMap<>();
        doctorVars.put("doctorName", doctorUser.getName());
        doctorVars.put("patientFullName", patientUser.getName());
        doctorVars.put("appointmentTime", formattedTime);
        doctorVars.put("isVirtual", true);
        doctorVars.put("meetingLink", appointment.getMeetingLink());
        doctorVars.put("initialSymptoms", appointment.getInitialSymptoms());
        doctorVars.put("purposeOfConsultation", appointment.getPurposeOfConsultation());

        NotificationDto doctorNotification = NotificationDto.builder()
                .recipient(doctorUser.getEmail())
                .subject("DAS: New Appointment Booked")
                .templateName("doctor-appointment")
                .templateVariables(doctorVars)
                .build();

        // Dispatch doctor email using the low-level service
        notificationService.sendEmail(doctorNotification, doctorUser);
        log.info("Dispatched new appointment email for doctor: {}", doctorUser.getEmail());
    }

    private void sendAppointmentCancellation(Appointment appointment, User cancelingUser) {

        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();

        // Safety check to ensure the cancellingUser is involved
        boolean isOwner = patientUser.getId().equals(cancelingUser.getId()) || doctorUser.getId().equals(cancelingUser.getId());
        if (!isOwner) {
            log.error("Cancellation initiated by user not associated with appointment. User ID: {}", cancelingUser.getId());
            return;
        }

        String formattedTime = appointment.getStartTime().format(FORMATTER);
        String cancellingPartyName = cancelingUser.getName();


        // --- Common Variables for the Template ---
        Map<String, Object> baseVars = new HashMap<>();
        baseVars.put("cancellingPartyName", cancellingPartyName);
        baseVars.put("appointmentTime", formattedTime);
        baseVars.put("doctorName", appointment.getDoctor().getLastName());
        baseVars.put("patientFullName", patientUser.getName());

        // --- 1. Dispatch Email to Doctor ---
        Map<String, Object> doctorVars = new HashMap<>(baseVars);
        doctorVars.put("recipientName", doctorUser.getName());

        NotificationDto doctorNotification = NotificationDto.builder()
                .recipient(doctorUser.getEmail())
                .subject("DAS: Appointment Cancellation")
                .templateName("appointment-cancellation")
                .templateVariables(doctorVars)
                .build();

        notificationService.sendEmail(doctorNotification, doctorUser);
        log.info("Dispatched cancellation email to Doctor: {}", doctorUser.getEmail());


        // --- 2. Dispatch Email to Patient ---
        Map<String, Object> patientVars = new HashMap<>(baseVars);
        patientVars.put("recipientName", patientUser.getName());

        NotificationDto patientNotification = NotificationDto.builder()
                .recipient(patientUser.getEmail())
                .subject("DAS: Appointment CANCELED (ID: " + appointment.getId() + ")")
                .templateName("appointment-cancellation")
                .templateVariables(patientVars)
                .build();

        notificationService.sendEmail(patientNotification, patientUser);
        log.info("Dispatched cancellation email to Patient: {}", patientUser.getEmail());


    }
}
