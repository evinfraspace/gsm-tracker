package com.gsmtracker.position;

import java.time.Duration;

public record TripSegmentationParams(
        Duration maxGap,
        double speedThresholdMps,
        Duration minStopDuration,
        double maxAccuracyMeters,
        double minTripDistanceMeters,
        Duration minTripDuration
) {

    public static TripSegmentationParams defaults() {
        return new TripSegmentationParams(
                Duration.ofMinutes(5),
                0.83,                   // ~3 км/ч в м/с
                Duration.ofMinutes(3),
                50.0,
                300.0,
                Duration.ofMinutes(2)
        );
    }

    public TripSegmentationParams withMaxGap(Duration newMaxGap) {
        return new TripSegmentationParams(newMaxGap, speedThresholdMps, minStopDuration,
                maxAccuracyMeters, minTripDistanceMeters, minTripDuration);
    }
}