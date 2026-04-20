package org.nttdata.apps.appointment.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.nttdata.apps.appointment.services.impl.HolidayService;

@Path("/holidays")
public class HolidaysController {

    @Inject
    private HolidayService holidayService;

    @GET
    public Response getHolidays(){
        return Response.ok(this.holidayService.getHolidaysFromPeru()).build();
    }
}
