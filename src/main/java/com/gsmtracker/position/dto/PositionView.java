package com.gsmtracker.position.dto;

import com.gsmtracker.position.Position;

import java.time.Instant;

public record PositionView(
        Long id,
        double lat,
        double lon,
        Double speed,
        Double accuracy,
        Double altitude,
        Double bearing,
        Integer battery,
        Instant recordedAt,
        Instant receivedAt
) {

    public static PositionView from(Position p) {
        return new PositionView(
                p.getId(),
                p.getLat(),
                p.getLon(),
                p.getSpeed(),
                p.getAccuracy(),
                p.getAltitude(),
                p.getBearing(),
                p.getBattery(),
                p.getRecordedAt(),
                p.getReceivedAt()
        );
    }
}
