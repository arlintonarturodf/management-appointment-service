package org.nttdata.apps.appointment.config.producer;


import org.nttdata.apps.appointment.avro.AppointmentEvent;

public interface AppointmentEventProducer {
    void sendAppointmentEvent(AppointmentEvent appointmentEvent);
}
