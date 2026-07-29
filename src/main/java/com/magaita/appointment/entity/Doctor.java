package com.magaita.appointment.entity;


import com.magaita.appointment.enums.Specialization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="doctors")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @Column(name = "license_number")
    private String licenseNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",unique = true)
    private User user;

    @OneToMany(mappedBy = "doctor",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Appointment> appointments;
}

