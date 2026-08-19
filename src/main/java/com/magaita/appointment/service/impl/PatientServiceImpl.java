package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.PatientDto;
import com.magaita.appointment.entity.Patient;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.enums.BloodGroup;
import com.magaita.appointment.enums.Genotype;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.PatientRepo;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.PatientService;
import com.magaita.appointment.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final UserService userService;
    private final PatientRepo patientRepo;
    private final ModelMapper modelMapper;

    @Override
    public Response<PatientDto> getPatientProfile() {
        User user = userService.getCurrentUser();

        Patient patient = patientRepo.findByUser(user)
                .orElseThrow(()->new NotFoundException("Patient not found!"));

        PatientDto patientDto = modelMapper.map(patient,PatientDto.class);

        return Response.<PatientDto>builder()
                .statusCode(200)
                .message("Patient profile retrieved successfully!")
                .data(patientDto)
                .build();
    }

    @Override
    public Response<?> updatePatientProfile(PatientDto patientDto) {
        User user = userService.getCurrentUser();

        Patient patient = patientRepo.findByUser(user)
                .orElseThrow(()->new NotFoundException("Patient not found!"));

        // Basic fields (firstName, lastName,)
        if (StringUtils.hasText(patientDto.getFirstName())) {
            patient.setFirstName(patientDto.getFirstName());
        }
        if (StringUtils.hasText(patientDto.getLastName())) {
            patient.setLastName(patientDto.getLastName());
        }
        if (StringUtils.hasText(patientDto.getPhone())) {
            patient.setPhone(patientDto.getPhone());
        }

        Optional.ofNullable(patientDto.getDateOfBirth()).ifPresent(patient::setDateOfBirth);

        // Medical fields (knownAllergies, bloodGroup, genotype)
        if (StringUtils.hasText(patientDto.getKnownAllergies())) {
            patient.setKnownAllergies(patientDto.getKnownAllergies());
        }

        // Enum fields (BloodGroup, Genotype)
        Optional.ofNullable(patientDto.getBloodGroup()).ifPresent(patient::setBloodGroup);
        Optional.ofNullable(patientDto.getGenotype()).ifPresent(patient::setGenotype);

        patientRepo.save(patient);

        return Response.builder()
                .statusCode(200)
                .message("Patient profile updated successfully.")
                .build();
    }

    @Override
    public Response<PatientDto> getPatientById(Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found with ID: " + patientId));

        PatientDto patientDTO = modelMapper.map(patient, PatientDto.class);

        return Response.<PatientDto>builder()
                .statusCode(200)
                .message("Patient retrieved successfully.")
                .data(patientDTO)
                .build();
    }

    @Override
    public Response<List<BloodGroup>> getAllBloodGroupEnums() {
        List<BloodGroup> bloodGroups = Arrays.asList(BloodGroup.values());

        return Response.<List<BloodGroup>>builder()
                .statusCode(200)
                .message("BloodGroups retrieved successfully")
                .data(bloodGroups)
                .build();
    }

    @Override
    public Response<List<Genotype>> getAllGenotypeEnums() {
        List<Genotype> genotypes = Arrays.asList(Genotype.values());

        return Response.<List<Genotype>>builder()
                .statusCode(200)
                .message("Genotype retrieved successfully")
                .data(genotypes)
                .build();
    }
}
