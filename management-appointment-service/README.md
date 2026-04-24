# Management Appointment Service

**Servicio de Gestión de Citas Médicas** construido con Quarkus, PostgreSQL, Kafka y Avro. Una aplicación empresarial de alta performance para la administración integral de citas médicas con eventos en tiempo real.


---

## 📚 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Tecnologías](#tecnologías)
- [Ejecución](#ejecución)
- [Endpoints API](#endpoints-api)
- [Modelos de Datos](#modelos-de-datos)

---

## 🎯 Descripción General

**Management Appointment Service** es un microservicio especializado en la gestión de citas médicas que ofrece:

✅ CRUD completo de citas médicas  
✅ Validación de feriados contra API externa (Nager.at)  
✅ Event Sourcing mediante Kafka + Avro  
✅ Persistencia relacional con Hibernate ORM + PostgreSQL  
✅ Arquitectura orientada a eventos  
✅ Microsegundos de latencia gracias a Quarkus  

---

## 🛠 Tecnologías

| Componente | Versión |
|-----------|---------|
| **Quarkus** | 3.34.3 |
| **Java** | 25 |
| **PostgreSQL** | 16-Alpine |
| **Kafka** | 7.5.0 |
| **Avro** | 7.2.0 |

---

## 📋 Ejecución Rápida

### Modo Desarrollo

```bash
./mvnw quarkus:dev
```

**Acceso:**
- API: `http://localhost:8080/api/v1/`
- Dev UI: `http://localhost:8080/q/dev/`

### Modo Producción

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```


---

## 🔌 Endpoints API

### Base URL: `http://localhost:8080/api/v1`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/appointments` | Obtener todas las citas |
| GET | `/appointments/{id}` | Obtener cita por ID |
| POST | `/appointments/create` | Crear nueva cita |
| PATCH | `/appointments/update/{id}` | Actualizar cita |
| DELETE | `/appointments/delete/{id}` | Eliminar cita |
| GET | `/holidays` | Obtener feriados de Perú |

### Crear Cita

```bash
curl -X POST http://localhost:8080/api/v1/appointments/create \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "550e8400-e29b-41d4-a716-446655440001",
    "doctorId": "550e8400-e29b-41d4-a716-446655440002",
    "scheduleId": "550e8400-e29b-41d4-a716-446655440003",
    "appointmentDateTime": "2026-05-20T14:00:00",
    "status": "CREATED",
    "reason": "Consulta"
  }'
```

---

## 💾 Modelos de Datos

### Entidad: Appointment

```java
@Entity
@Table(name = "tb_appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID scheduleId;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;    // CREATED, CONFIRMED, CANCELLED
    private String reason;
    private LocalDateTime createdAt;
}
```

### Estados

```
CREATED    → Recién creada
CONFIRMED  → Confirmada
CANCELLED  → Cancelada
```

---

## ⚙️ Configuración

### application.yml

```yaml
quarkus:
  application:
    name: appointment-service
  http:
    port: 8080
    root-path: /api/v1/
  datasource:
    username: postgres
    password: 123456789
    jdbc:
      url: jdbc:postgresql://localhost:5432/db_appointment

kafka:
  bootstrap:
    servers: localhost:9092
  schema:
    registry:
      url: http://localhost:8081

appointment:
  kafka:
    topic-name: appointment-events
```

---

## 📊 Infraestructura

### Docker Compose

```bash
docker-compose up -d
```

**Servicios:**
- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Schema Registry: `localhost:8081`
- Control Center: `localhost:9021`

---

## 📡 Eventos Kafka

**Tópicos:**
- `appointment-events` - Eventos de citas (3 particiones)
- `appointment-events.DLT` - Dead Letter Topic

**Eventos:**
- `APPOINTMENT_CREATED`
- `APPOINTMENT_UPDATED`
- `APPOINTMENT_DELETED`

---

## ✅ Validaciones

- ✓ Fecha debe ser futura
- ✓ NO puede ser feriado de Perú
- ✓ Campos requeridos: patientId, doctorId, scheduleId, appointmentDateTime, status

---
