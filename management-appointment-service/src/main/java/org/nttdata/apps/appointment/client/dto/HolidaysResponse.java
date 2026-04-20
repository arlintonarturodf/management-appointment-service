package org.nttdata.apps.appointment.client.dto;

import java.util.List;

public record HolidaysResponse(
       String date,
       String localName,
       String name,
       String countryCode,
       boolean fixed,
       boolean global,
       List<String> counties,
       String launchYear,
       List<String>types
) {
}
