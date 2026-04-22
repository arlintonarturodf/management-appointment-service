package org.nttdata.apps.appointment.resources.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.nttdata.apps.appointment.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentRequest(


        UUID patientId,

        UUID doctorId,

        UUID scheduleId,


        @Future(message = "La fecha de cita debe ser válida.")
        LocalDateTime appointmentDateTime,

        AppointmentStatus status,

        String reason

) {
}
