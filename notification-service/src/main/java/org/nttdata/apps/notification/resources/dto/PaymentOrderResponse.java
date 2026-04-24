package org.nttdata.apps.notification.resources.dto;

import org.nttdata.apps.notification.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentOrderResponse(
        UUID id,
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime appointmentDateTime,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
