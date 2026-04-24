package org.nttdata.apps.appointment.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.inject.Inject;

import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.nttdata.apps.appointment.config.producer.AppointmentEventProducer;

import org.nttdata.apps.appointment.avro.AppointmentEvent;

import java.util.Properties;

@Slf4j
@ApplicationScoped
public class KafkaProducerProducer implements AppointmentEventProducer {

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @ConfigProperty(name = "kafka.schema.registry.url")
    String schemaRegistryUrl;

    @ConfigProperty(name = "appointment.kafka.topic-name")
    String topic;

    @Inject
    KafkaProducer<String, Object> kafkaClient;

    // ─── CDI Factory: Quarkus instanciará y gestionará este Producer ────────────
    @Produces
    @ApplicationScoped
    public KafkaProducer<String, Object> createKafkaClient() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        // Garantiza entrega exactamente-una-vez
        props.put(ProducerConfig.ACKS_CONFIG,               "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new KafkaProducer<>(props);
    }

    // ─── Envío del evento al tópico de citas ────────────────────────────────────
    @Override
    public void sendAppointmentEvent(AppointmentEvent event) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topic, event.getId().toString(), event);

        kafkaClient.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Error enviando evento a Kafka: {}", exception.getMessage());
            } else {
                log.info("✅ Evento [{}] enviado → tópico: {} | partición: {}",
                        event.getEventType(), metadata.topic(), metadata.partition());
            }
        });
    }

    // ─── Cierre limpio del producer al apagar la app ────────────────────────────
    public void stop(@Disposes KafkaProducer<String, Object> producer) {
        log.info("🛑 Cerrando Kafka Producer...");
        producer.close();
    }
}
