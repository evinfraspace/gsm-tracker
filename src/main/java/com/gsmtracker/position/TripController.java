package com.gsmtracker.position;

import com.gsmtracker.position.dto.TrackGeoJson;
import com.gsmtracker.position.dto.Trip;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class TripController {

    private static final Duration DEFAULT_WINDOW = Duration.ofHours(24);
    private static final long DEFAULT_GAP_MINUTES = 5;

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/{id}/trips")
    public List<Trip> trips(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "" + DEFAULT_GAP_MINUTES) long gapMinutes) {
        Instant toInstant = parseOr(to, Instant.now());
        Instant fromInstant = parseOr(from, toInstant.minus(DEFAULT_WINDOW));
        TripSegmentationParams params = TripSegmentationParams.defaults()
                .withMaxGap(Duration.ofMinutes(gapMinutes));
        return tripService.getTrips(id, fromInstant, toInstant, params);
    }

    @GetMapping("/{id}/trips/{index}/track")
    public TrackGeoJson tripTrack(
            @PathVariable Long id,
            @PathVariable int index,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "" + DEFAULT_GAP_MINUTES) long gapMinutes) {
        Instant toInstant = parseOr(to, Instant.now());
        Instant fromInstant = parseOr(from, toInstant.minus(DEFAULT_WINDOW));
        TripSegmentationParams params = TripSegmentationParams.defaults()
                .withMaxGap(Duration.ofMinutes(gapMinutes));
        return tripService.getTripTrack(id, fromInstant, toInstant, params, index);
    }

    private Instant parseOr(String value, Instant fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Instant.parse(value);
    }
}