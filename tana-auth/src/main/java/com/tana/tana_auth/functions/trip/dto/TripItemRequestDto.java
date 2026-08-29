package com.tana.tana_auth.functions.trip.dto;

import lombok.Data;

@Data
public class TripItemRequestDto {
    private String itemType;
    private String itemId;
    private Boolean saved;
    private Boolean going;
}
