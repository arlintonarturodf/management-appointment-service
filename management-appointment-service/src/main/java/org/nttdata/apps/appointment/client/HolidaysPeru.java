package org.nttdata.apps.appointment.client;

import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.nttdata.apps.appointment.client.dto.HolidaysResponse;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v3/PublicHolidays/{Year}/{CountryCode}")
@RegisterRestClient(configKey = "holidays-api")
public interface HolidaysPeru {


    @GET
    @Produces(APPLICATION_JSON)
    List<HolidaysResponse> holidaysFromPeru(@PathParam("Year") int year,@PathParam("CountryCode") String countryCode);

}
