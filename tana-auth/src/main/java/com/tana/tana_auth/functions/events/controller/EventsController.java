package com.tana.tana_auth.functions.events.controller;

import com.tana.tana_auth.functions.events.dto.EventResponseDto;
import com.tana.tana_auth.functions.events.service.EventsService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/events")
public class EventsController {

    @Autowired
    private EventsService eventsService;

    @GetMapping
    public TanaApiResponse getEvents(@RequestParam(required = false) String months) {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(eventsService.getEvents(normalizeMonths(months)))
            .build();
    }

    @GetMapping("/{eventSlug}")
    public TanaApiResponse getEvent(@PathVariable String eventSlug) {
        EventResponseDto event = eventsService.getEvent(eventSlug);
        return TanaApiResponse.builder()
            .isSuccess(event != null)
            .resultData(event)
            .build();
    }

    private String normalizeMonths(String months) {
        if (months == null || months.isBlank()) {
            return "all";
        }

        String normalized = Arrays.stream(months.split(","))
            .map(String::trim)
            .filter(month -> !month.isBlank())
            .map(month -> month.toLowerCase(Locale.ROOT))
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));

        return normalized.isBlank() ? "all" : normalized;
    }
}
