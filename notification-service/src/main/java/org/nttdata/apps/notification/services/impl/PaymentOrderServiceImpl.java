package org.nttdata.apps.notification.services.impl;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.nttdata.apps.notification.entity.PaymentOrder;
import org.nttdata.apps.notification.entity.enums.PaymentStatus;
import org.nttdata.apps.notification.repository.PaymentOrderRepository;
import org.nttdata.apps.notification.services.PaymentOrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.nttdata.apps.appointment.avro.AppointmentEvent;


@Slf4j
@ApplicationScoped
public class PaymentOrderServiceImpl implements PaymentOrderService {

    @Inject
    PaymentOrderRepository paymentOrderRepository;

    // ── APPOINTMENT_CREATED → crear orden PENDING ─────────────────────────────
    @Override
    @Transactional
    public void createOrder(AppointmentEvent event) {
        UUID appointmentId = UUID.fromString(event.getId().toString());

        // Idempotencia: si ya existe una orden para esta cita, no crear otra
        paymentOrderRepository.findByAppointmentId(appointmentId).ifPresent(existing -> {
            log.warn("⚠️ Ya existe una orden de pago para la cita: {}", appointmentId);
            throw new RuntimeException("Orden de pago duplicada para appointmentId: " + appointmentId);
        });

        PaymentOrder order = PaymentOrder.builder()
                .appointmentId(appointmentId)
                .patientId(UUID.fromString(event.getPatientId().toString()))
                .doctorId(UUID.fromString(event.getDoctorId().toString()))
                .appointmentDateTime(LocalDateTime.parse(event.getAppointmentDateTime().toString()))
                .status(PaymentStatus.PENDING)
                .amount(resolveAmountByDoctor(event.getDoctorId().toString()))
                .currency("PEN")
                .build();

        paymentOrderRepository.persist(order);
        log.info("✅ Orden de pago PENDING creada para cita: {} | monto: {} PEN",
                appointmentId, order.getAmount());
    }

    // ── APPOINTMENT_UPDATED → actualizar fecha y/o monto ─────────────────────
    @Override
    @Transactional
    public void updateOrder(AppointmentEvent event) {
        UUID appointmentId = UUID.fromString(event.getId().toString());

        PaymentOrder order = paymentOrderRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() -> {
                    log.error("❌ No existe orden de pago para la cita: {}", appointmentId);
                    return new RuntimeException("Orden de pago no encontrada: " + appointmentId);
                });

        // Solo actualizar si la orden no fue ya pagada
        if (PaymentStatus.PAID.equals(order.getStatus())) {
            log.warn("⚠️ La orden {} ya fue pagada, no se actualiza.", order.getId());
            return;
        }

        UUID newDoctorId = UUID.fromString(event.getDoctorId().toString());
        BigDecimal newAmount = resolveAmountByDoctor(newDoctorId.toString());

        order.setDoctorId(newDoctorId);
        order.setAppointmentDateTime(LocalDateTime.parse(event.getAppointmentDateTime().toString()));
        order.setAmount(newAmount);

        paymentOrderRepository.persist(order);
        log.info("✅ Orden de pago actualizada para cita: {} | nuevo monto: {} PEN",
                appointmentId, newAmount);
    }

    // ── APPOINTMENT_DELETED → cancelar orden ─────────────────────────────────
    @Override
    @Transactional
    public void cancelOrder(AppointmentEvent event) {
        UUID appointmentId = UUID.fromString(event.getId().toString());

        PaymentOrder order = paymentOrderRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() -> {
                    log.error("❌ No existe orden de pago para la cita: {}", appointmentId);
                    return new RuntimeException("Orden de pago no encontrada: " + appointmentId);
                });

        // Solo cancelar si aún está pendiente, no revertir pagos ya realizados
        if (PaymentStatus.PAID.equals(order.getStatus())) {
            log.warn("⚠️ La orden {} ya fue pagada, no se puede cancelar automáticamente.", order.getId());
            return;
        }

        order.setStatus(PaymentStatus.CANCELLED);
        paymentOrderRepository.persist(order);
        log.info("✅ Orden de pago CANCELADA para cita: {}", appointmentId);
    }

    /**
     * Calcula el monto según el doctor.
     * TODO: Reemplazar con llamada al microservicio de doctores (doctor-service)
     *       para obtener la tarifa real de cada especialista.
     */
    private BigDecimal resolveAmountByDoctor(String doctorId) {
        log.warn("⚠️ resolveAmountByDoctor(): usando monto base para doctorId={}", doctorId);
        return new BigDecimal("150.00");
    }


}
