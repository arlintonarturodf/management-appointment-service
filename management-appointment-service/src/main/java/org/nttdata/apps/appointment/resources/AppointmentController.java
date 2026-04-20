package org.nttdata.apps.appointment.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.nttdata.apps.appointment.entity.Appointment;
import org.nttdata.apps.appointment.mapper.AppointmentMapper;
import org.nttdata.apps.appointment.resources.dto.AppointmentRequest;
import org.nttdata.apps.appointment.resources.dto.AppointmentResponse;
import org.nttdata.apps.appointment.services.impl.AppointmentServiceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/appointments")
public class AppointmentController {

    @Inject
    private AppointmentServiceImpl appointmentService;

    @Inject
    private AppointmentMapper appointmentMapper;


    @GET
    @Path("/{id}")
    public AppointmentResponse getById(@PathParam("id") UUID uuid){
        return this.appointmentService.getAppointmentById(uuid);
    }

    @GET
    public List<AppointmentResponse> getAll(){
     return    this.appointmentService.getAllAppointments()
                .stream()
                .map(appointmentMapper::toResponse).collect(Collectors.toList());
    }

    @POST
    @Path("/create")
    public Response create(@Valid AppointmentRequest appointmentRequest){
     Appointment appointment =
             this.appointmentService.createAppointment(this.appointmentMapper.toEntity(appointmentRequest));
      return   Response.status(Response.Status.CREATED)
              .entity(appointment)
              .build();
    }

    @PATCH
    @Path("/update")
    public Response update(@PathParam("id") UUID id,@Valid AppointmentRequest appointmentRequest){

    }


}
