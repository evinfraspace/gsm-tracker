package com.gsmtracker.position;

import com.gsmtracker.position.dto.PositionView;
import com.gsmtracker.position.dto.TrackGeoJson;
import org.springframework.http.ResponseEntity;
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
public class PositionQueryController {

    private static final Duration DEFAULT_WINDOW = Duration.ofHours(24);

    private final PositionService positionService;

    public PositionQueryController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/{id}/positions/latest")
    public ResponseEntity<PositionView> latest(@PathVariable Long id) {
        return positionService.getLatest(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/positions")
    public List<PositionView> history(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int limit) {
        Instant toInstant = parseOr(to, Instant.now());
        Instant fromInstant = parseOr(from, toInstant.minus(DEFAULT_WINDOW));
        return positionService.getHistory(id, fromInstant, toInstant, limit);
    }

    @GetMapping("/{id}/track")
    public TrackGeoJson track(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Instant toInstant = parseOr(to, Instant.now());
        Instant fromInstant = parseOr(from, toInstant.minus(DEFAULT_WINDOW));
        return positionService.getTrack(id, fromInstant, toInstant);
    }

    private Instant parseOr(String value, Instant fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Instant.parse(value);
    }
}
