# notification-service

Servicio responsable de notificaciones y gestión básica de órdenes de pago para citas médicas.

Este proyecto está desarrollado con Quarkus y realiza las siguientes funciones principales:

- Consumir eventos Avro de tipo AppointmentEvent desde Kafka (topic: appointment-events).
- Procesar eventos: APPOINTMENT_CREATED, APPOINTMENT_UPDATED, APPOINTMENT_DELETED.
- Enviar notificaciones por email al paciente (Quarkus Mailer).
- Crear/actualizar/cancelar órdenes de pago en una base de datos PostgreSQL (Hibernate ORM / Panache).
- Enviar mensajes al Dead Letter Topic (DLT) cuando ocurre un fallo al procesar un evento.


## Contenido del README

1. Requisitos
2. Configuración (valores por defecto y claves importantes)
3. Ejecutar en desarrollo
4. Empaquetado y ejecución
5. Dependencias externas necesarias (Kafka, Schema Registry, Postgres, MailHog/SMTP)
6. Endpoints REST disponibles
7. Estructura del proyecto y ficheros relevantes
8. Tests y verificación
9. Notas de operación y troubleshooting

## 1) Requisitos

- Java 17+ (pom.xml configura release 25; ajustar según tu JDK)  <-- revisar versión local
- Maven (se incluye el wrapper `./mvnw`)
- Apache Kafka
- Confluent Schema Registry (si consumes Avro desde un registry)
- PostgreSQL (o usar contenedor Docker)
- Un servidor SMTP para envío de emails (MailHog recomendable en desarrollo)

## 2) Configuración (valores por defecto)

La configuración principal está en `src/main/resources/application.yml`. Las claves más relevantes:

- quarkus.http.port: 8090
- quarkus.http.root-path: /api/v1/
- quarkus.datasource.jdbc.url: jdbc:postgresql://localhost:5433/db_notification
- quarkus.datasource.username/password: postgres / 123456789
- quarkus.mailer.host: localhost
- quarkus.mailer.port: 1025
- quarkus.mailer.from: no-reply@appointments.com
- quarkus.mailer.mock: false (poner true en desarrollo para evitar envíos reales)
- kafka.bootstrap.servers: localhost:9092
- kafka.schema.registry.url: http://localhost:8081
- kafka.consumer.group-id: notification-group
- kafka.consumer.topic: appointment-events
- kafka.consumer.dlt-topic: appointment-events.DLT


## 3) Ejecutar en desarrollo

Usa el wrapper de Maven para arrancar en modo dev (live coding, Quarkus Dev UI):

```bash
./mvnw quarkus:dev
```

Dev UI (solo en modo dev): http://localhost:8080/q/dev/

Antes de arrancar en dev asegúrate de tener corriendo los servicios externos (Kafka + Schema Registry + Postgres + MailHog) o ajusta la configuración para usar servicios remotos.

## 4) Empaquetado y ejecución

Construir el artefacto:

```bash
./mvnw package
```

Ejecutar (jar):

```bash
java -jar target/quarkus-app/quarkus-run.jar
```


Crear binario nativo (opcional):

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## 5) Dependencias externas recomendadas (arranque rápido con Docker)

Ejemplo mínimo (ejecutar cada contenedor en terminales separadas o crear tu docker-compose):

- Zookeeper + Kafka (Confluent/Bitnami images)
- Confluent Schema Registry (si consumes Avro)
- PostgreSQL
- MailHog (para capturar emails en dev)

Comandos rápidos (ejemplos):

```bash
# Postgres
docker run --name notification-postgres -e POSTGRES_PASSWORD=123456789 -e POSTGRES_USER=postgres -e POSTGRES_DB=db_notification -p 5433:5432 -d postgres:15

# MailHog
 docker run --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog

# (Recomendado) Confluent Platform con Kafka y Schema Registry – usar un docker-compose oficial para arrancar todos los servicios.
```

Nota: ajustar puertos en `application.yml` si usas otros puertos en los contenedores.

## 6) Endpoints REST disponibles

El servicio expone recursos REST (prefijo: /api/v1):

- GET /api/v1/payment-orders
  - Lista todas las órdenes de pago.
  - Respuesta: JSON array de objetos PaymentOrderResponse.

- GET /api/v1/payment-orders/{id}
  - Obtiene una orden por su ID (UUID).

- GET /api/v1/payment-orders/appointment/{appointmentId}
  - Obtiene la orden asociada a una cita (appointmentId: UUID).

Ejemplo:

```bash
curl http://localhost:8090/api/v1/payment-orders
curl http://localhost:8090/api/v1/payment-orders/<UUID>
```

Los endpoints están implementados en `org.nttdata.apps.notification.resources.PaymentOrderController`.

## 7) Kafka, Avro y DLT

- Topic consumido: `appointment-events` (configurable en `application.yml`).
- Schema Avro: `src/main/avro/appointment-event.avsc` — el proyecto genera clases Avro en la compilación.
- El consumidor principal es `AppointmentEventConsumer`:
  - Realiza un poll loop manual de Kafka y procesa cada registro según `eventType`.
  - Para cada evento:
	- APPOINTMENT_CREATED → enviar email + crear orden PENDING
	- APPOINTMENT_UPDATED → enviar email + actualizar orden
	- APPOINTMENT_DELETED → enviar email + cancelar orden
  - En caso de error, el evento se envía al Dead Letter Topic (`appointment-events.DLT`) mediante `DTLProducer`.

## 8) Base de datos

- Entidad principal: `PaymentOrder` (en `org.nttdata.apps.notification.entity`).
- Repositorio: `PaymentOrderRepository` (Panache repository) para consultas comunes.
- Hibernate está configurado para `generation: update` por defecto (ver `application.yml`).

Importante: en entornos de producción revisar la política de generación de esquema y backups.

## 9) Envío de emails (simulacion de envio) hasta integración con patient-service

- Implementado en `NotificationServiceImpl` usando `io.quarkus.mailer.Mailer`.
- Actualmente el email del paciente se resuelve con un placeholder (`paciente@example.com`) — hay un TODO que indica integrar con `patient-service`.
- En desarrollo se recomienda usar MailHog en el puerto 1025; la UI web de MailHog está en http://localhost:8025


## 10) Estructura relevante del proyecto

- src/main/avro/appointment-event.avsc  → esquema Avro del evento de cita
- src/main/java/org/nttdata/apps/notification/consumer/AppointmentEventConsumer.java
- src/main/java/org/nttdata/apps/notification/config/DTLProducer.java
- src/main/java/org/nttdata/apps/notification/services/impl/NotificationServiceImpl.java
- src/main/java/org/nttdata/apps/notification/services/impl/PaymentOrderServiceImpl.java
- src/main/java/org/nttdata/apps/notification/resources/PaymentOrderController.java
- src/main/resources/application.yml

## 11) Troubleshooting y recomendaciones

- Si no llegan mensajes Avro: verifica `kafka.bootstrap.servers` y `kafka.schema.registry.url`.
- Si el consumidor no arranca: revisa logs del poll loop en `AppointmentEventConsumer`.
- Para depurar envíos de correo, activa `quarkus.mailer.mock=true` o usa MailHog.
- Para evitar duplicados en órdenes: `PaymentOrderServiceImpl.createOrder` hace una comprobación idempotente.

