package org.nttdata.apps.appointment.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.nttdata.apps.appointment.entity.Appointment;

import java.util.UUID;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepositoryBase<Appointment, UUID> {

}
