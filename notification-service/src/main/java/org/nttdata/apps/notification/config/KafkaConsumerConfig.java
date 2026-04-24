package org.nttdata.apps.notification.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.nttdata.apps.appointment.avro.AppointmentEvent;

import java.util.Collections;
import java.util.Properties;




@Slf4j
@ApplicationScoped
public class KafkaConsumerConfig {
    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @ConfigProperty(name = "kafka.schema.registry.url")
    String schemaRegistryUrl;

    @ConfigProperty(name = "kafka.consumer.group-id")
    String groupId;

    @ConfigProperty(name = "kafka.consumer.topic")
    String topic;

    @Produces
    @ApplicationScoped
    public KafkaConsumer<String, AppointmentEvent> createKafkaConsumer() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,           groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,  "earliest");

        // Commit manual para garantizar que el mensaje fue procesado antes de confirmarlo
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,   10);

        // Deserializadores Avro con Schema Registry
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                KafkaAvroDeserializer.class.getName());
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl);

        // Deserializar directamente a la clase AppointmentEvent (no GenericRecord)
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        KafkaConsumer<String, AppointmentEvent> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        log.info("🎧 KafkaConsumer suscrito al tópico: {} | grupo: {}", topic, groupId);

        return consumer;
    }

    public void stop(@Disposes KafkaConsumer<String, AppointmentEvent> consumer) {
        log.info("🛑 Cerrando Kafka Consumer...");
        consumer.close();
    }
}
