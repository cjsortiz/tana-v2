package com.tana.tana_auth.functions.events.service.impl;

import com.tana.tana_auth.functions.events.dto.EventResponseDto;
import com.tana.tana_auth.functions.events.dto.EventNearbySpotDto;
import com.tana.tana_auth.functions.events.repository.EventsRepository;
import com.tana.tana_auth.functions.events.service.EventsService;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_common.model.EventsMaster;
import com.tana.tana_common.model.PlaceMaster;
import com.tana.tana_common.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventsServiceImpl implements EventsService {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private PlacesRepository placesRepository;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    @Cacheable(value = "events-list", key = "#months")
    public List<EventResponseDto> getEvents(String months) {
        Set<String> monthFilters = parseMonths(months);

        return eventsRepository.findAll()
            .stream()
            .filter(event -> monthFilters.isEmpty()
                || "all".equalsIgnoreCase(event.getMonthKey())
                || monthFilters.contains(Optional.ofNullable(event.getMonthKey()).orElse("").toLowerCase()))
            .sorted(
                Comparator
                    .comparing(EventsMaster::getStartsOn, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(EventsMaster::getEventName, Comparator.nullsLast(String::compareToIgnoreCase))
            )
            .map(this::toDto)
            .toList();
    }

    @Override
    public EventResponseDto getEvent(String eventSlug) {
        return eventsRepository.findByEventSlug(eventSlug)
            .map(this::toDto)
            .orElse(null);
    }

    private EventResponseDto toDto(EventsMaster event) {
        return EventResponseDto.builder()
            .id(event.getId())
            .eventSlug(event.getEventSlug())
            .eventName(event.getEventName())
            .category(event.getCategory())
            .dateLabel(event.getDateLabel())
            .monthSort(event.getMonthSort())
            .monthKey(event.getMonthKey())
            .location(event.getLocation())
            .gps(event.getGps())
            .frequency(event.getFrequency())
            .about(event.getAbout())
            .tanaTip(event.getTanaTip())
            .badge(event.getBadge())
            .website(event.getWebsite())
            .hiddenTags(event.getHiddenTags())
            .accentColor(event.getAccentColor())
            .tagBg(event.getTagBg())
            .tagTextColor(event.getTagTextColor())
            .featured(event.getFeatured())
            .nearbySpots(findNearbySpots(event))
            .build();
    }

    private Set<String> parseMonths(String months) {
        if (months == null || months.isBlank() || "all".equalsIgnoreCase(months)) {
            return Set.of();
        }

        return Arrays.stream(months.split(","))
            .map(String::trim)
            .filter(month -> !month.isBlank())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    private List<EventNearbySpotDto> findNearbySpots(EventsMaster event) {
        Optional<double[]> eventPoint = parsePoint(event.getGps());
        if (eventPoint.isEmpty()) {
            return List.of();
        }

        return placesRepository.findAll()
            .stream()
            .map(place -> toNearbyCandidate(place, eventPoint.get()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparingDouble(NearbyCandidate::distanceKm))
            .limit(3)
            .map(NearbyCandidate::spot)
            .toList();
    }

    private Optional<NearbyCandidate> toNearbyCandidate(PlaceMaster place, double[] eventPoint) {
        Optional<double[]> placePoint = parsePoint(place.getGpsLocation());
        if (placePoint.isEmpty()) {
            return Optional.empty();
        }

        double distanceKm = haversineKm(
            eventPoint[0],
            eventPoint[1],
            placePoint.get()[0],
            placePoint.get()[1]);

        List<String> images = commonUtils.getSpotImages(place.getName());
        String image = images.isEmpty() ? null : images.get(0);

        return Optional.of(new NearbyCandidate(
            distanceKm,
            EventNearbySpotDto.builder()
                .placeId(place.getId())
                .name(place.getName())
                .town(place.getTown())
                .mainCategory(place.getMainCategoryTypeEnum() == null
                    ? null
                    : place.getMainCategoryTypeEnum().getTypeString())
                .distanceLabel(formatDistance(distanceKm))
                .image(image)
                .build()));
    }

    private Optional<double[]> parsePoint(String point) {
        if (point == null || !point.contains(",")) {
            return Optional.empty();
        }

        String[] parts = point.split(",");
        if (parts.length < 2) {
            return Optional.empty();
        }

        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            return Optional.of(new double[]{lat, lng});
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2)
            * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return Math.round(distanceKm * 1000) + " m";
        }

        return String.format("%.1f km", distanceKm);
    }

    private record NearbyCandidate(double distanceKm, EventNearbySpotDto spot) {
    }
}
