package org.nttdata.apps.appointment.services;

import org.nttdata.apps.appointment.entity.Appointment;
import org.nttdata.apps.appointment.resources.dto.AppointmentRequest;
import org.nttdata.apps.appointment.resources.dto.AppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse getAppointmentById(UUID uuid);
    List<Appointment> getAllAppointments();
    AppointmentResponse createAppointment(AppointmentRequest appointmentRequest);
    AppointmentResponse updatedAppointment(UUID uuid, AppointmentRequest appointmentRequest);
    boolean deleteAppointment(UUID uuid);

}
