package com.tana.tana_auth.functions.trip.service;

import com.tana.tana_auth.functions.trip.dto.TripDatesRequestDto;
import com.tana.tana_auth.functions.trip.dto.TripItemRequestDto;
import com.tana.tana_auth.functions.trip.dto.TripResponseDto;
import com.tana.tana_common.constant.exception.TanaException;

public interface TripService {
    TripResponseDto getTrip() throws TanaException;
    TripResponseDto saveDates(TripDatesRequestDto requestDto) throws TanaException;
    TripResponseDto saveItem(TripItemRequestDto requestDto) throws TanaException;
}
