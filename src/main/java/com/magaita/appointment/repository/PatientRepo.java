package com.magaita.appointment.repository;

import com.magaita.appointment.entity.Patient;
import com.magaita.appointment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepo extends JpaRepository<Patient,Long> {
   Optional<Patient>findByUser(User user);
}
