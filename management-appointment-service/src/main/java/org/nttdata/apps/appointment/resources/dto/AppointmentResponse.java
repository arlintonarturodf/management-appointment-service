package org.nttdata.apps.appointment.resources.dto;

import lombok.Builder;
import org.nttdata.apps.appointment.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        Long scheduleId,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status,
        String reason,
        LocalDateTime createdAt
) {
}
