package com.magaita.appointment.service;

import com.magaita.appointment.dto.PatientDto;
import com.magaita.appointment.enums.BloodGroup;
import com.magaita.appointment.enums.Genotype;
import com.magaita.appointment.res.Response;

import java.util.List;

public interface PatientService {
    Response<PatientDto> getPatientProfile();

    Response<?> updatePatientProfile(PatientDto patientDto);

    Response<PatientDto> getPatientById(Long patientId);

    Response<List<BloodGroup>> getAllBloodGroupEnums();

    Response<List<Genotype>> getAllGenotypeEnums();
}
