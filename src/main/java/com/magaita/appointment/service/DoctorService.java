package com.magaita.appointment.service;

import com.magaita.appointment.dto.DoctorDto;
import com.magaita.appointment.enums.Specialization;
import com.magaita.appointment.res.Response;

import java.util.List;

public interface DoctorService {
    Response<DoctorDto> getDoctorProfile();

    Response<?> updateDoctorProfile(DoctorDto doctorDTO);

    Response<List<DoctorDto>> getAllDoctors();

    Response<DoctorDto> getDoctorById(Long doctorId);

    Response<List<DoctorDto>> searchDoctorsBySpecialization(Specialization specialization);

    Response<List<Specialization>> getAllSpecializationEnums();
}
