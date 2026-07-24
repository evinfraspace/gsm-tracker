package com.gsmtracker.position.dto;

import com.gsmtracker.position.GeoDistance;
import com.gsmtracker.position.Position;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record Trip(
        int index,
        Instant startedAt,
        Instant endedAt,
        long durationSeconds,
        int pointCount,
        double distanceMeters,
        double avgSpeedMps,
        double maxSpeedMps,
        double startLat,
        double startLon,
        double endLat,
        double endLon
) {

    public static Trip of(int index, List<Position> segment) {
        Position first = segment.getFirst();
        Position last = segment.getLast();
        long duration = Duration.between(first.getRecordedAt(), last.getRecordedAt()).getSeconds();
        double distance = GeoDistance.pathLengthMeters(segment);
        double avgSpeed = duration > 0 ? distance / duration : 0.0;
        return new Trip(
                index,
                first.getRecordedAt(),
                last.getRecordedAt(),
                duration,
                segment.size(),
                distance,
                avgSpeed,
                maxReportedSpeed(segment),
                first.getLat(), first.getLon(),
                last.getLat(), last.getLon()
        );
    }

    private static double maxReportedSpeed(List<Position> segment) {
        double max = 0.0;
        for (Position p : segment) {
            if (p.getSpeed() != null && p.getSpeed() > max) {
                max = p.getSpeed();
            }
        }
        return max;
    }
}