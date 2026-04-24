package org.nttdata.apps.notification.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;

@Slf4j
@ApplicationScoped
public class DTLProducer {
    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @ConfigProperty(name = "kafka.consumer.dlt-topic")
    String dltTopic;

    @Produces
    @ApplicationScoped
    public KafkaProducer<String, String> createDLTProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaProducer<>(props);
    }

    public void sendToDLT(KafkaProducer<String, String> dltProducer,
                          String key, String errorMessage) {
        ProducerRecord<String, String> record = new ProducerRecord<>(dltTopic, key, errorMessage);
        dltProducer.send(record, (meta, ex) -> {
            if (ex != null) {
                log.error("❌ Error al enviar al DLT: {}", ex.getMessage());
            } else {
                log.warn("⚠️ Evento enviado al DLT: {} | partición: {}", dltTopic, meta.partition());
            }
        });
    }

    public void stop(@Disposes KafkaProducer<String, String> producer) {
        log.info("🛑 Cerrando DLT Producer...");
        producer.close();
    }
}
