package com.gsmtracker.position;

import com.gsmtracker.common.TripNotFoundException;
import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceService;
import com.gsmtracker.position.dto.Trip;
import com.gsmtracker.position.dto.TrackGeoJson;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TripService {

    private final PositionRepository positionRepository;
    private final DeviceService deviceService;
    private final TripSegmenter tripSegmenter;

    public TripService(PositionRepository positionRepository,
                       DeviceService deviceService,
                       TripSegmenter tripSegmenter) {
        this.positionRepository = positionRepository;
        this.deviceService = deviceService;
        this.tripSegmenter = tripSegmenter;
    }

    @Transactional(readOnly = true)
    public List<Trip> getTrips(Long deviceId, Instant from, Instant to, TripSegmentationParams params) {
        List<List<Position>> segments = keptSegments(deviceId, from, to, params);
        List<Trip> result = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            result.add(Trip.of(i + 1, segments.get(i)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public TrackGeoJson getTripTrack(Long deviceId, Instant from, Instant to,
                                     TripSegmentationParams params, int index) {
        List<List<Position>> segments = keptSegments(deviceId, from, to, params);
        if (index < 1 || index > segments.size()) {
            throw new TripNotFoundException(deviceId, index);
        }
        List<Position> segment = segments.get(index - 1);

        double[][] coordinates = segment.stream()
                .map(p -> new double[]{p.getLon(), p.getLat()}) // GeoJSON: [lon, lat]
                .toArray(double[][]::new);

        Map<String, Object> properties = Map.of(
                "deviceId", deviceId,
                "tripIndex", index,
                "points", segment.size());

        return TrackGeoJson.lineString(coordinates, properties);
    }

    /** Единый источник сегментов: и список, и геометрия строятся из него -> индексы совпадают. */
    private List<List<Position>> keptSegments(Long deviceId, Instant from, Instant to,
                                              TripSegmentationParams params) {
        Device device = deviceService.getByIdOrThrow(deviceId);
        List<Position> points = positionRepository.findByDeviceAndRecordedAtBetweenOrderByRecordedAtAsc(
                device, from, to, Pageable.unpaged());

        List<List<Position>> segments = tripSegmenter.segment(points, params);

        List<List<Position>> kept = new ArrayList<>();
        for (List<Position> segment : segments) {
            Trip trip = Trip.of(0, segment);
            if (trip.distanceMeters() >= params.minTripDistanceMeters()
                    && trip.durationSeconds() >= params.minTripDuration().getSeconds()) {
                kept.add(segment);
            }
        }
        return kept;
    }
}