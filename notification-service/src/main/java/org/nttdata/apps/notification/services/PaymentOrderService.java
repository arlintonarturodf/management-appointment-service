package org.nttdata.apps.notification.services;

import org.nttdata.apps.appointment.avro.AppointmentEvent;


public interface PaymentOrderService {

    void createOrder(AppointmentEvent event);
    void updateOrder(AppointmentEvent event);
    void cancelOrder(AppointmentEvent event);
}
