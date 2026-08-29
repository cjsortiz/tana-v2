package com.tana.tana_auth.functions.trip.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TripResponseDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String dateLabel;
    private List<String> savedRouteIds;
    private List<String> savedEventIds;
    private List<String> goingEventIds;
}
