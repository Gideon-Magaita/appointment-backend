package com.magaita.appointment.repository;

import com.magaita.appointment.entity.Doctor;
import com.magaita.appointment.entity.User;
import com.magaita.appointment.enums.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor,Long> {

    Optional<Doctor>findByUser(User user);

    List<Doctor> findBySpecialization(Specialization specialization);
}
