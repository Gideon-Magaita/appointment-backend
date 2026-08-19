package com.magaita.appointment.controller;

import com.magaita.appointment.dto.DoctorDto;
import com.magaita.appointment.enums.Specialization;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<Response<DoctorDto>> getDoctorProfile(){
        return ResponseEntity.ok(doctorService.getDoctorProfile());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<Response<?>> updateDoctorProfile(@RequestBody DoctorDto doctorDTO){
        return ResponseEntity.ok(doctorService.updateDoctorProfile(doctorDTO));
    }

    @GetMapping
    public ResponseEntity<Response<List<DoctorDto>>> getAllDoctors(){
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<Response<DoctorDto>> getDoctorById(@PathVariable Long doctorId){
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @GetMapping("/filter")
    public ResponseEntity<Response<List<DoctorDto>>> searchDoctorsBySpecialization(
            @RequestParam(required = true) Specialization specialization
    ){
        return ResponseEntity.ok(doctorService.searchDoctorsBySpecialization(specialization));
    }

    @GetMapping("/specializations")
    public ResponseEntity<Response<List<Specialization>>> getAllSpecializationEnums(){
        return ResponseEntity.ok(doctorService.getAllSpecializationEnums());
    }

}
