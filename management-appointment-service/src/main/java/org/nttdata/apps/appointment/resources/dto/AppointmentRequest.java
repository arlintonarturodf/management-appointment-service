package org.nttdata.apps.appointment.resources.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.nttdata.apps.appointment.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentRequest(

        @NotBlank(message = "El campo de PatientId es obligatorio.")
        UUID patientId,

        @NotBlank(message = "El campo de DoctorId es obligatorio.")
        UUID doctorId,

        @NotBlank(message = "El campo de ScheduleId es obligatorio.")
        Long scheduleId,

        @NotBlank(message = "La fecha de la cita es obligatoria.")
        @Future(message = "La fecha de cita debe ser válida.")
        LocalDateTime appointmentDateTime,

        AppointmentStatus status,

        String reason

) {
}
