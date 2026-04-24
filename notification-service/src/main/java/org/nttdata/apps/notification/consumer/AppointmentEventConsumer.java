package org.nttdata.apps.notification.consumer;


import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.nttdata.apps.notification.config.DTLProducer;
import org.nttdata.apps.notification.services.NotificationService;
import org.nttdata.apps.notification.services.PaymentOrderService;

import org.nttdata.apps.appointment.avro.AppointmentEvent;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;




@Slf4j
@ApplicationScoped
public class AppointmentEventConsumer {

    @Inject
    KafkaConsumer<String, AppointmentEvent> kafkaConsumer;

    @Inject
    KafkaProducer<String, String> dltProducer;

    @Inject
    DTLProducer dltProducerBean;

    @Inject
    NotificationService notificationService;

    @Inject
    PaymentOrderService paymentOrderService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    // ── Arrancar el poll loop cuando levanta Quarkus ──────────────────────────
    void onStart(@Observes StartupEvent ev) {
        running.set(true);
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "kafka-consumer-thread");
            t.setDaemon(true);
            return t;
        });
        executor.submit(this::pollLoop);
        log.info("🚀 AppointmentEventConsumer iniciado.");
    }

    // ── Detener el poll loop al apagar Quarkus ────────────────────────────────
    void onStop(@Observes ShutdownEvent ev) {
        log.info("🛑 Deteniendo AppointmentEventConsumer...");
        running.set(false);
        executor.shutdown();
    }

    // ── Poll loop principal ───────────────────────────────────────────────────
    private void pollLoop() {
        log.info("🎧 Poll loop iniciado, esperando eventos...");

        while (running.get()) {
            try {
                ConsumerRecords<String, AppointmentEvent> records =
                        kafkaConsumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, AppointmentEvent> record : records) {
                    processRecord(record);
                }

                // Commit manual: solo después de procesar todos los registros del batch
                if (!records.isEmpty()) {
                    kafkaConsumer.commitSync();
                    log.debug("✅ Commit realizado para {} registros.", records.count());
                }

            } catch (Exception e) {
                log.error("❌ Error en el poll loop: {}", e.getMessage(), e);
            }
        }

        log.info("🛑 Poll loop finalizado.");
    }

    // ── Procesar un registro individual ──────────────────────────────────────
    private void processRecord(ConsumerRecord<String, AppointmentEvent> record) {
        AppointmentEvent event = record.value();
        String eventType = event.getEventType().toString();

        log.info("📨 Evento recibido → tipo: {} | appointmentId: {} | offset: {}",
                eventType, event.getId(), record.offset());

        try {
            switch (eventType) {
                case "APPOINTMENT_CREATED" -> handleCreated(event);
                case "APPOINTMENT_UPDATED" -> handleUpdated(event);
                case "APPOINTMENT_DELETED" -> handleDeleted(event);
                default -> log.warn("⚠️ eventType desconocido: {}", eventType);
            }
        } catch (Exception e) {
            log.error("❌ Error procesando evento [{}] para cita [{}]: {}",
                    eventType, event.getId(), e.getMessage());

            // Enviar al Dead Letter Topic para no perder el evento
            dltProducerBean.sendToDLT(
                    dltProducer,
                    event.getId().toString(),
                    buildDLTMessage(event, e)
            );
        }
    }

    // ── Handlers por tipo de evento ───────────────────────────────────────────

    private void handleCreated(AppointmentEvent event) {
        log.info("🟢 Procesando APPOINTMENT_CREATED: {}", event.getId());
        notificationService.notifyAppointmentCreated(event);   // email al paciente
        paymentOrderService.createOrder(event);                 // orden PENDING
    }

    private void handleUpdated(AppointmentEvent event) {
        log.info("🟡 Procesando APPOINTMENT_UPDATED: {}", event.getId());
        notificationService.notifyAppointmentUpdated(event);   // email de reprogramación
        paymentOrderService.updateOrder(event);                 // actualizar monto/fecha
    }

    private void handleDeleted(AppointmentEvent event) {
        log.info("🔴 Procesando APPOINTMENT_DELETED: {}", event.getId());
        notificationService.notifyAppointmentDeleted(event);   // email de cancelación
        paymentOrderService.cancelOrder(event);                 // orden CANCELLED
    }

    // ── DLT message builder ───────────────────────────────────────────────────
    private String buildDLTMessage(AppointmentEvent event, Exception e) {
        return """
                {
                  "appointmentId": "%s",
                  "eventType": "%s",
                  "error": "%s",
                  "timestamp": "%s"
                }
                """.formatted(
                event.getId(),
                event.getEventType(),
                e.getMessage(),
                java.time.LocalDateTime.now()
        );
    }

}
