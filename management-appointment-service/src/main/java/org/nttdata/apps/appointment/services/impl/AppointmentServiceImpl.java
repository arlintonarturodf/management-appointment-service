package org.nttdata.apps.appointment.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nttdata.apps.appointment.entity.Appointment;
import org.nttdata.apps.appointment.exception.BussinesException;
import org.nttdata.apps.appointment.mapper.AppointmentMapper;
import org.nttdata.apps.appointment.repository.AppointmentRepository;
import org.nttdata.apps.appointment.resources.dto.AppointmentRequest;
import org.nttdata.apps.appointment.resources.dto.AppointmentResponse;
import org.nttdata.apps.appointment.services.AppointmentService;

import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class AppointmentServiceImpl implements AppointmentService {

    @Inject
    private AppointmentRepository appointmentRepository;

    @Inject
    private AppointmentMapper appointmentMapper;

    @Inject
    private HolidayService holidayService;



    @Override
    public AppointmentResponse getAppointmentById(UUID uuid) {
        Appointment appointment = this.appointmentRepository.findById(uuid);

        return  this.appointmentMapper.toResponse(appointment);

    }

    @Override
    public List<Appointment> getAllAppointments() {
        return  this.appointmentRepository.listAll().stream().toList();
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest appointmentRequest) {
        log.info("Validando fecha de registro para cita: {}",appointmentRequest.appointmentDateTime());

        //verificar en mi servicio de holidays si la fecha ingresada existe en dias no laborables
        if ( this.holidayService.existDateValidInHolidaysFromPeru(appointmentRequest.appointmentDateTime())){
            throw new BussinesException("La fecha ingresada no es válida, es día laborable.");
        }
        log.info("Agregando nueva cita a base de datos: {} ",appointmentRequest.patientId());

        Appointment appointmentNew = this.appointmentMapper.toEntity(appointmentRequest);
        this.appointmentRepository.persist(appointmentNew);

        log.info("Cita agregada correctamente a base de datos: {}",appointmentNew);
        return this.appointmentMapper.toResponse(appointmentNew);
    }


    @Override
    @Transactional
    public AppointmentResponse updatedAppointment(UUID uuid, AppointmentRequest appointmentRequest) {

        //mapper de la request
        Appointment  appointmentFind = this.appointmentRepository.findById(uuid);
        if (appointmentFind ==null){
            throw new BussinesException("La cita no existe en la base de datos.");

        }
        log.info("Actualizando cita {}",appointmentRequest.patientId());

        Appointment appointment = this.appointmentMapper.toEntity(appointmentRequest);
        this.appointmentRepository.persist(appointment);

        log.info("Cita: {} actualizada correctamente ",appointment);

        return  this.appointmentMapper.toResponse(appointment);
    }



    @Override
    public boolean deleteAppointment(UUID uuid) {
        if (this.appointmentRepository.deleteById(uuid)){
            return true;
        };
        return false;
    }


}
