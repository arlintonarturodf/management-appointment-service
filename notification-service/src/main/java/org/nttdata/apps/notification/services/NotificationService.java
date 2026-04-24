package org.nttdata.apps.notification.services;

import org.nttdata.apps.appointment.avro.AppointmentEvent;


public interface NotificationService {
    void notifyAppointmentCreated(AppointmentEvent event);
    void notifyAppointmentUpdated(AppointmentEvent event);
    void notifyAppointmentDeleted(AppointmentEvent event);

}
