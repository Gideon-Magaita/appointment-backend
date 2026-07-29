package com.magaita.appointment.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;

@Entity
@Table(name="consultations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime consultationDate;

    @Column(name = "subjective_notes")
    @Lob
    private String subjectiveNotes;

    @Column(name = "objective_findings")
    @Lob
    private String objectiveFindings;

    @Lob
    private String assessment;

    @Lob
    private String plan;

    @OneToOne
    @JoinColumn(name = "appointment_id",unique = true,nullable = false)
    private Appointment appointment;


}
