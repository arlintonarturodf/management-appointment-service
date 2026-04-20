package org.nttdata.apps.appointment.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.nttdata.apps.appointment.entity.Appointment;
import org.nttdata.apps.appointment.resources.dto.AppointmentRequest;
import org.nttdata.apps.appointment.resources.dto.AppointmentResponse;

@ApplicationScoped
public class AppointmentMapper {


    public Appointment toEntity(AppointmentRequest appointmentRequest){
        return Appointment.builder()
                .patientId(appointmentRequest.patientId())
                .doctorId(appointmentRequest.doctorId())
                .scheduleId(appointmentRequest.scheduleId())
                .appointmentDateTime(appointmentRequest.appointmentDateTime())
                .status(appointmentRequest.status())
                .reason(appointmentRequest.reason())
                .build();
    }

    public AppointmentResponse toResponse(Appointment appointment){
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .scheduleId(appointment.getScheduleId())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
