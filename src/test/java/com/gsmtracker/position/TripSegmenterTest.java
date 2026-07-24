package com.gsmtracker.position;

import com.gsmtracker.device.Device;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TripSegmenterTest {

    private final TripSegmenter segmenter = new TripSegmenter();
    private final Device device = new Device("Test", "token");

    private final Duration gap = Duration.ofMinutes(5);
    private final double stopSpeed = 1.0;              // м/с
    private final Duration minStop = Duration.ofMinutes(3);

    private Position p(String iso, double lat, double lon, Double speed, Double accuracy) {
        Position pos = new Position(device, lat, lon);
        pos.setRecordedAt(Instant.parse(iso));
        pos.setSpeed(speed);
        pos.setAccuracy(accuracy);
        return pos;
    }

    // ---- разрыв по времени (Шаг 1) ----

    @Test
    void emptyInputProducesNoTrips() {
        assertThat(segmenter.splitByTimeGap(List.of(), gap)).isEmpty();
    }

    @Test
    void largeGapSplitsIntoTwoTrips() {
        List<List<Position>> segments = segmenter.splitByTimeGap(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:02:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:22:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:24:00Z", 55.0, 37.0, 10.0, 5.0)
        ), gap);
        assertThat(segments).hasSize(2);
    }

    @Test
    void gapExactlyEqualToThresholdStaysOneTrip() {
        List<List<Position>> segments = segmenter.splitByTimeGap(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:05:00Z", 55.0, 37.0, 10.0, 5.0)
        ), gap);
        assertThat(segments).hasSize(1);
    }

    // ---- фильтр по точности ----

    @Test
    void filtersOutInaccuratePointsButKeepsNullAccuracy() {
        List<Position> clean = segmenter.filterByAccuracy(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:01:00Z", 55.0, 37.0, 10.0, 90.0),   // выброс
                p("2026-07-21T10:02:00Z", 55.0, 37.0, 10.0, null)    // без точности — оставляем
        ), 50.0);
        assertThat(clean).hasSize(2);
    }

    // ---- разбиение по стоянкам (Шаг 2) ----

    @Test
    void longStopSplitsTripInTwo() {
        List<List<Position>> trips = segmenter.splitByStops(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:02:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:04:00Z", 55.0, 37.0, 0.0, 5.0),   // стоянка...
                p("2026-07-21T10:08:00Z", 55.0, 37.0, 0.0, 5.0),   // ...4 мин >= 3
                p("2026-07-21T10:10:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:12:00Z", 55.0, 37.0, 10.0, 5.0)
        ), stopSpeed, minStop);

        assertThat(trips).hasSize(2);
        assertThat(trips.get(0)).hasSize(2);   // стоячие точки отброшены
        assertThat(trips.get(1)).hasSize(2);
    }

    @Test
    void shortPauseStaysInsideTrip() {
        List<List<Position>> trips = segmenter.splitByStops(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:02:00Z", 55.0, 37.0, 0.0, 5.0),   // пауза...
                p("2026-07-21T10:03:00Z", 55.0, 37.0, 0.0, 5.0),   // ...1 мин < 3
                p("2026-07-21T10:05:00Z", 55.0, 37.0, 10.0, 5.0)
        ), stopSpeed, minStop);

        assertThat(trips).hasSize(1);
        assertThat(trips.getFirst()).hasSize(4);   // пауза сохранена
    }

    @Test
    void stationaryStartPointIsKept() {
        List<List<Position>> trips = segmenter.splitByStops(List.of(
                p("2026-07-21T10:00:00Z", 55.0, 37.0, 0.0, 5.0),   // старт со скоростью 0
                p("2026-07-21T10:02:00Z", 55.0, 37.0, 10.0, 5.0),
                p("2026-07-21T10:04:00Z", 55.0, 37.0, 10.0, 5.0)
        ), stopSpeed, minStop);

        assertThat(trips).hasSize(1);
        assertThat(trips.getFirst()).hasSize(3);
    }
}