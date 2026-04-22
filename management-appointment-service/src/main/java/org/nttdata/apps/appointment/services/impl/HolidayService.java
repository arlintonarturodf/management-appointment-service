package org.nttdata.apps.appointment.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Convert;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.nttdata.apps.appointment.client.HolidaysPeru;
import org.nttdata.apps.appointment.client.dto.HolidaysResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@ApplicationScoped
public class HolidayService {


    private final int YEAR =2026;
    private final String COUNTRY="PE";

    @Inject
    @RestClient
    HolidaysPeru holidaysPeru;

    public List<HolidaysResponse> getHolidaysFromPeru(){
        log.info("Listando dias laborables en Perú: ");
        return  this.holidaysPeru.holidaysFromPeru(YEAR,COUNTRY);
    }

    public boolean existDateValidInHolidaysFromPeru(LocalDateTime localDateTime){
        //convertir a string
        String dateToString = localDateTime.toString();
        //separar por espacio ' ' y dividir fecha y hora
        String[] dateAndHours  =  dateToString.split("T");
        //guardar la fecha aparte y la hora aparte
        String date=dateAndHours[0];
        String hours = dateAndHours[1];
        //obtienes este formato: 2026-01-01

        //llamar al servicio de holidays para buscar si esa fecha es dia no laborable

       List<HolidaysResponse> arrayWithDatesHolidays = this.getHolidaysFromPeru();

      return arrayWithDatesHolidays.stream().anyMatch(hol -> hol.date().equals(date));

    }
}
