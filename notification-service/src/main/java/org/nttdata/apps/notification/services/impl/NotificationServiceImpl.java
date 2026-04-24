package org.nttdata.apps.notification.services.impl;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.nttdata.apps.notification.services.NotificationService;

import org.nttdata.apps.appointment.avro.AppointmentEvent;


@Slf4j
@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {


    @Inject
    Mailer mailer;

    // ── APPOINTMENT_CREATED ───────────────────────────────────────────────────
    @Override
    public void notifyAppointmentCreated(AppointmentEvent event) {
        log.info("📧 Enviando notificación de CREACIÓN al paciente: {}", event.getPatientId());

        String subject = "✅ Tu cita médica fue confirmada";
        String body = buildCreatedBody(event);

        sendEmail(resolvePatientEmail(event.getPatientId().toString()), subject, body);
    }

    // ── APPOINTMENT_UPDATED ───────────────────────────────────────────────────
    @Override
    public void notifyAppointmentUpdated(AppointmentEvent event) {
        log.info("📧 Enviando notificación de ACTUALIZACIÓN al paciente: {}", event.getPatientId());

        String subject = "🔄 Tu cita médica fue reprogramada";
        String body = buildUpdatedBody(event);

        sendEmail(resolvePatientEmail(event.getPatientId().toString()), subject, body);
    }

    // ── APPOINTMENT_DELETED ───────────────────────────────────────────────────
    @Override
    public void notifyAppointmentDeleted(AppointmentEvent event) {
        log.info("📧 Enviando notificación de CANCELACIÓN al paciente: {}", event.getPatientId());

        String subject = "❌ Tu cita médica fue cancelada";
        String body = buildDeletedBody(event);

        sendEmail(resolvePatientEmail(event.getPatientId().toString()), subject, body);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        // Validación básica del destinatario
        if (to == null || to.isBlank() || !to.contains("@") || !to.contains(".")) {
            log.warn("⚠️ Email destinatario inválido, se omite envío: '{}'", to);
            return;
        }

        final int maxAttempts = 3;
        int attempt = 0;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                mailer.send(
                        Mail.withHtml(to, subject, body)
                );
                log.info("✅ Email enviado correctamente a: {} (intento {})", to, attempt);
                return; // éxito
            } catch (Exception e) {
                // Registrar con stacktrace completo para diagnóstico
                log.error("❌ Error al enviar email a {} en intento {}: {}", to, attempt, e.getMessage(), e);
                // Si quedan reintentos, esperar un breve intervalo antes de reintentar
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting to retry email send");
                        break;
                    }
                } else {
                    // Después de todos los reintentos, no lanzamos excepción para
                    // evitar que un fallo SMTP haga que el procesamiento del evento
                    // completo termine en el DLT. Registramos el fallo para operación.
                    log.error("❌ No fue posible enviar el email a {} tras {} intentos. Se continúa con el procesamiento.", to, maxAttempts);
                }
            }
        }
    }

    private String buildCreatedBody(AppointmentEvent event) {
        return """
                <html><body>
                <h2>Tu cita médica fue confirmada</h2>
                <p>Estimado paciente,</p>
                <p>Tu cita ha sido registrada exitosamente con los siguientes datos:</p>
                <ul>
                    <li><b>ID de cita:</b> %s</li>
                    <li><b>Fecha y hora:</b> %s</li>
                    <li><b>Doctor ID:</b> %s</li>
                    <li><b>Motivo:</b> %s</li>
                </ul>
                <p>Por favor, preséntate 15 minutos antes de tu cita.</p>
                <p>Saludos,<br/>Sistema de Gestión de Citas</p>
                </body></html>
                """.formatted(
                event.getId(),
                event.getAppointmentDateTime(),
                event.getDoctorId(),
                event.getReason() != null ? event.getReason() : "No especificado"
        );
    }

    private String buildUpdatedBody(AppointmentEvent event) {
        return """
                <html><body>
                <h2>Tu cita médica fue reprogramada</h2>
                <p>Estimado paciente,</p>
                <p>Tu cita ha sido actualizada con los siguientes datos:</p>
                <ul>
                    <li><b>ID de cita:</b> %s</li>
                    <li><b>Nueva fecha y hora:</b> %s</li>
                    <li><b>Doctor ID:</b> %s</li>
                    <li><b>Estado:</b> %s</li>
                </ul>
                <p>Saludos,<br/>Sistema de Gestión de Citas</p>
                </body></html>
                """.formatted(
                event.getId(),
                event.getAppointmentDateTime(),
                event.getDoctorId(),
                event.getStatus()
        );
    }

    private String buildDeletedBody(AppointmentEvent event) {
        return """
                <html><body>
                <h2>Tu cita médica fue cancelada</h2>
                <p>Estimado paciente,</p>
                <p>Lamentamos informarte que tu cita ha sido cancelada:</p>
                <ul>
                    <li><b>ID de cita:</b> %s</li>
                    <li><b>Fecha programada:</b> %s</li>
                </ul>
                <p>Si deseas reagendar, comunícate con nosotros.</p>
                <p>Saludos,<br/>Sistema de Gestión de Citas</p>
                </body></html>
                """.formatted(
                event.getId(),
                event.getAppointmentDateTime()
        );
    }

    /**
     * Resuelve el email del paciente a partir de su ID.
     * TODO: Reemplazar con llamada al microservicio de pacientes (patient-service)
     *       o con una tabla local de pacientes replicada vía Kafka.
     */
    private String resolvePatientEmail(String patientId) {
        // Placeholder: en producción consultar patient-service REST o DB local
        log.warn("⚠️ resolvePatientEmail(): usando email placeholder para patientId={}", patientId);
        return "paciente@example.com";
    }

}
