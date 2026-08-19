package com.magaita.appointment.controller;


import com.magaita.appointment.dto.PatientDto;
import com.magaita.appointment.enums.BloodGroup;
import com.magaita.appointment.enums.Genotype;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;


    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Response<PatientDto>>getPatientProfile(){
        return ResponseEntity.ok(patientService.getPatientProfile());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Response<?>>updatePatientProfile(@RequestBody PatientDto patientDto){
        return ResponseEntity.ok(patientService.updatePatientProfile(patientDto));
    }
    @GetMapping("/{patientId}")
    public ResponseEntity<Response<PatientDto>> getPatientById(@PathVariable Long patientId){
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @GetMapping("/bloodgroup")
    public ResponseEntity<Response<List<BloodGroup>>> getAllBloodGroupEnums(){
        return ResponseEntity.ok(patientService.getAllBloodGroupEnums());
    }

    @GetMapping("/genotype")
    public ResponseEntity<Response<List<Genotype>>> getAllGenotypeEnums(){
        return ResponseEntity.ok(patientService.getAllGenotypeEnums());
    }

}
