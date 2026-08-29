package com.tana.tana_auth.functions.trip.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TripDatesRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String dateLabel;
}
