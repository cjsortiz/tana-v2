package com.tana.tana_auth.functions.trip.controller;

import com.tana.tana_auth.functions.trip.dto.TripDatesRequestDto;
import com.tana.tana_auth.functions.trip.dto.TripItemRequestDto;
import com.tana.tana_auth.functions.trip.service.TripService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/trip")
public class TripController {

    @Autowired
    private TripService tripService;

    @GetMapping
    public TanaApiResponse getTrip() throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(tripService.getTrip())
            .build();
    }

    @PostMapping("/dates")
    public TanaApiResponse saveDates(@RequestBody TripDatesRequestDto requestDto) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(tripService.saveDates(requestDto))
            .build();
    }

    @PostMapping("/items")
    public TanaApiResponse saveItem(@RequestBody TripItemRequestDto requestDto) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(tripService.saveItem(requestDto))
            .build();
    }
}
