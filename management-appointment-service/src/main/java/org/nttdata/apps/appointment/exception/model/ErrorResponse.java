package org.nttdata.apps.appointment.exception.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private String message;
    private String code;
    private LocalDateTime timestamp;

}
