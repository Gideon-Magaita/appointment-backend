package com.magaita.appointment.controller;

import com.magaita.appointment.dto.AppointmentDto;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Response<AppointmentDto>> bookAppointment(@RequestBody @Valid AppointmentDto appointmentDto){
        return ResponseEntity.ok(appointmentService.bookAppointment(appointmentDto));
    }

    @GetMapping
    public ResponseEntity<Response<List<AppointmentDto>>> getMyAppointments(){
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<Response<?>> cancelAppointment(@PathVariable Long appointmentId){
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
    }

    @PutMapping("/complete/{appointmentId}")
    @PreAuthorize(("hasAuthority('DOCTOR')"))
    public ResponseEntity<Response<?>> completeAppointment(@PathVariable Long appointmentId){
        return ResponseEntity.ok(appointmentService.completeAppointment(appointmentId));
    }

}
