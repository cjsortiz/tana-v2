package com.tana.tana_auth.functions.events.service;

import com.tana.tana_auth.functions.events.dto.EventResponseDto;

import java.util.List;

public interface EventsService {
    List<EventResponseDto> getEvents(String months);

    EventResponseDto getEvent(String eventSlug);
}
