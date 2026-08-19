package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.DoctorDto;
import com.magaita.appointment.entity.Doctor;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.enums.Specialization;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.DoctorRepo;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.DoctorService;
import com.magaita.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    private final UserService userService;
    private final DoctorRepo doctorRepo;
    private final ModelMapper modelMapper;

    @Override
    public Response<DoctorDto> getDoctorProfile() {
        User user = userService.getCurrentUser();

        Doctor doctor = doctorRepo.findByUser(user)
                .orElseThrow(()-> new NotFoundException("Doctor Not Found"));

        return Response.<DoctorDto>builder()
                .statusCode(200)
                .message("Doctor profile retrieved successfully.")
                .data(modelMapper.map(doctor, DoctorDto.class))
                .build();
    }

    @Override
    public Response<?> updateDoctorProfile(DoctorDto doctorDTO) {
        User currentUser = userService.getCurrentUser();

        Doctor doctor = doctorRepo.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("Doctor profile not found."));

        // Basic fields (firstName, lastName)
        if (StringUtils.hasText(doctorDTO.getFirstName())) {
            doctor.setFirstName(doctorDTO.getFirstName());
        }
        if (StringUtils.hasText(doctorDTO.getLastName())) {
            doctor.setLastName(doctorDTO.getLastName());
        }

        Optional.ofNullable(doctorDTO.getSpecialization()).ifPresent(doctor::setSpecialization);

        doctorRepo.save(doctor);
        log.info("Doctor Profile updated");

        return Response.builder()
                .statusCode(200)
                .message("Doctor profile updated successfully.")
                .build();
    }

    @Override
    public Response<List<DoctorDto>> getAllDoctors() {
        List<Doctor> doctors = doctorRepo.findAll();

        List<DoctorDto> doctorDTOS = doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDto.class))
                .toList();

        return Response.<List<DoctorDto>>builder()
                .statusCode(200)
                .message("All doctors retrieved successfully.")
                .data(doctorDTOS)
                .build();
    }

    @Override
    public Response<DoctorDto> getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return Response.<DoctorDto>builder()
                .statusCode(200)
                .message("Doctor retrieved successfully.")
                .data(modelMapper.map(doctor, DoctorDto.class))
                .build();
    }

    @Override
    public Response<List<DoctorDto>> searchDoctorsBySpecialization(Specialization specialization) {
        List<Doctor> doctors = doctorRepo.findBySpecialization(specialization);

        List<DoctorDto> doctorDTOs= doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDto.class))
                .toList();

        String message = doctors.isEmpty() ?
                "No doctors found for specialization: " + specialization.name() :
                "Doctors retrieved successfully for specialization: " + specialization.name();


        return Response.<List<DoctorDto>>builder()
                .statusCode(200)
                .message(message)
                .data(doctorDTOs)
                .build();
    }

    @Override
    public Response<List<Specialization>> getAllSpecializationEnums() {
        List<Specialization> specializations = Arrays.asList(Specialization.values());

        return Response.<List<Specialization>>builder()
                .statusCode(200)
                .message("Specializations retrieved successfully")
                .data(specializations)
                .build();
    }
}
