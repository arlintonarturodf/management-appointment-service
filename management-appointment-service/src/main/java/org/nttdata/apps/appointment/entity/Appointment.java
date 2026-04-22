package org.nttdata.apps.appointment.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.nttdata.apps.appointment.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Entity
@Table(name = "tb_appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID doctorId;

    @Column(nullable = false)
    private UUID scheduleId;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false,updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;




}
