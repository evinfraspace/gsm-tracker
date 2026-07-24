package com.gsmtracker.position;

import com.gsmtracker.common.TripNotFoundException;
import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceService;
import com.gsmtracker.position.dto.TrackGeoJson;
import com.gsmtracker.position.dto.Trip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DeviceService deviceService;

    private final Device device = new Device("Test", "token");

    private final Instant from = Instant.parse("2026-07-21T00:00:00Z");
    private final Instant to = Instant.parse("2026-07-22T00:00:00Z");

    private TripService tripService() {
        return new TripService(positionRepository, deviceService, new TripSegmenter());
    }

    private TripSegmentationParams params() {
        return new TripSegmentationParams(
                Duration.ofMinutes(5),   // maxGap
                1.0,                     // speedThreshold м/с
                Duration.ofMinutes(3),   // minStop
                50.0,                    // maxAccuracy
                100.0,                   // minTripDistance м
                Duration.ofSeconds(60)   // minTripDuration
        );
    }

    private Position p(String iso, double lat, double lon, Double speed, Double accuracy) {
        Position pos = new Position(device, lat, lon);
        pos.setRecordedAt(Instant.parse(iso));
        pos.setSpeed(speed);
        pos.setAccuracy(accuracy);
        return pos;
    }

    private void stub(List<Position> points) {
        when(deviceService.getByIdOrThrow(1L)).thenReturn(device);
        when(positionRepository.findByDeviceAndRecordedAtBetweenOrderByRecordedAtAsc(
                eq(device), eq(from), eq(to), any())).thenReturn(points);
    }

    @Test
    void enrichesTripWithDistanceAndSpeed() {
        stub(List.of(
                p("2026-07-21T10:00:00Z", 55.7500, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:03:00Z", 55.7600, 37.6100, 10.0, 5.0) // ~1112 м за 180 с
        ));

        List<Trip> trips = tripService().getTrips(1L, from, to, params());

        assertThat(trips).hasSize(1);
        Trip t = trips.get(0);
        assertThat(t.index()).isEqualTo(1);
        assertThat(t.pointCount()).isEqualTo(2);
        assertThat(t.durationSeconds()).isEqualTo(180);
        assertThat(t.distanceMeters()).isCloseTo(1112.0, within(50.0));
        assertThat(t.avgSpeedMps()).isCloseTo(1112.0 / 180.0, within(0.5));
        assertThat(t.maxSpeedMps()).isEqualTo(10.0);
    }

    @Test
    void dropsJunkTripAndReindexes() {
        stub(List.of(
                // нормальная поездка
                p("2026-07-21T10:00:00Z", 55.7500, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:03:00Z", 55.7600, 37.6100, 10.0, 5.0),
                // разрыв > 5 мин -> вторая поездка, но она "мусорная" (1 м, 60 с)
                p("2026-07-21T10:20:00Z", 55.7600, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:21:00Z", 55.76001, 37.6100, 10.0, 5.0)
        ));

        List<Trip> trips = tripService().getTrips(1L, from, to, params());

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).index()).isEqualTo(1);
    }

    @Test
    void innerStopSplitsIntoTwoTrips() {
        stub(List.of(
                p("2026-07-21T10:00:00Z", 55.7500, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:02:00Z", 55.7520, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:04:00Z", 55.7520, 37.6100, 0.0, 5.0),   // стоянка 4 мин
                p("2026-07-21T10:08:00Z", 55.7520, 37.6100, 0.0, 5.0),
                p("2026-07-21T10:10:00Z", 55.7540, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:12:00Z", 55.7560, 37.6100, 10.0, 5.0)
        ));

        List<Trip> trips = tripService().getTrips(1L, from, to, params());

        assertThat(trips).hasSize(2);
        assertThat(trips.get(0).index()).isEqualTo(1);
        assertThat(trips.get(1).index()).isEqualTo(2);
    }

    @Test
    void returnsNoTripsWhenNoPositions() {
        stub(List.of());
        assertThat(tripService().getTrips(1L, from, to, params())).isEmpty();
    }

    @Test
    void tripTrackReturnsPointsOfSelectedTrip() {
        stub(List.of(
                p("2026-07-21T10:00:00Z", 55.7500, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:03:00Z", 55.7600, 37.6100, 10.0, 5.0),
                // разрыв > 5 мин -> вторая поездка
                p("2026-07-21T10:20:00Z", 55.7000, 37.5000, 10.0, 5.0),
                p("2026-07-21T10:23:00Z", 55.7100, 37.5000, 10.0, 5.0)
        ));

        TrackGeoJson track = tripService().getTripTrack(1L, from, to, params(), 2);

        assertThat(track.geometry().type()).isEqualTo("LineString");
        assertThat(track.geometry().coordinates()).hasDimensions(2, 2);
        // порядок GeoJSON: [lon, lat]
        assertThat(track.geometry().coordinates()[0][0]).isEqualTo(37.5000);
        assertThat(track.geometry().coordinates()[0][1]).isEqualTo(55.7000);
        assertThat(track.properties()).containsEntry("tripIndex", 2);
    }

    @Test
    void tripTrackThrowsWhenIndexOutOfRange() {
        stub(List.of(
                p("2026-07-21T10:00:00Z", 55.7500, 37.6100, 10.0, 5.0),
                p("2026-07-21T10:03:00Z", 55.7600, 37.6100, 10.0, 5.0)
        ));

        assertThatThrownBy(() -> tripService().getTripTrack(1L, from, to, params(), 5))
                .isInstanceOf(TripNotFoundException.class);
    }
}