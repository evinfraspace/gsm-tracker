package com.gsmtracker.position;

import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceService;
import com.gsmtracker.position.dto.BatchResponse;
import com.gsmtracker.position.dto.PositionRequest;
import com.gsmtracker.position.dto.PositionResponse;
import com.gsmtracker.position.dto.PositionView;
import com.gsmtracker.position.dto.TrackGeoJson;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PositionService {

    private static final int MAX_HISTORY_LIMIT = 5000;

    private final PositionRepository positionRepository;
    private final DeviceService deviceService;

    public PositionService(PositionRepository positionRepository, DeviceService deviceService) {
        this.positionRepository = positionRepository;
        this.deviceService = deviceService;
    }

    @Transactional
    public PositionResponse ingestSingle(Device device, PositionRequest request) {
        if (positionRepository.existsByDeviceAndRecordedAt(device, request.recordedAt())) {
            return new PositionResponse(null, "duplicate");
        }
        Position saved = positionRepository.save(toEntity(device, request));
        return new PositionResponse(saved.getId(), "stored");
    }

    @Transactional
    public BatchResponse ingestBatch(Device device, List<PositionRequest> points) {
        int accepted = 0;
        int duplicates = 0;
        for (PositionRequest request : points) {
            if (positionRepository.existsByDeviceAndRecordedAt(device, request.recordedAt())) {
                duplicates++;
                continue;
            }
            positionRepository.save(toEntity(device, request));
            accepted++;
        }
        return new BatchResponse(accepted, duplicates);
    }

    @Transactional(readOnly = true)
    public Optional<PositionView> getLatest(Long deviceId) {
        Device device = deviceService.getByIdOrThrow(deviceId);
        return positionRepository.findFirstByDeviceOrderByRecordedAtDesc(device)
                .map(PositionView::from);
    }

    @Transactional(readOnly = true)
    public List<PositionView> getHistory(Long deviceId, Instant from, Instant to, int limit) {
        Device device = deviceService.getByIdOrThrow(deviceId);
        Pageable pageable = PageRequest.of(0, clampLimit(limit), Sort.by("recordedAt").ascending());
        return positionRepository
                .findByDeviceAndRecordedAtBetweenOrderByRecordedAtAsc(device, from, to, pageable)
                .stream()
                .map(PositionView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrackGeoJson getTrack(Long deviceId, Instant from, Instant to) {
        Device device = deviceService.getByIdOrThrow(deviceId);
        List<Position> points = positionRepository.findByDeviceAndRecordedAtBetweenOrderByRecordedAtAsc(
                device, from, to, Pageable.unpaged());

        double[][] coordinates = points.stream()
                .map(p -> new double[]{p.getLon(), p.getLat()}) // GeoJSON: [lon, lat]
                .toArray(double[][]::new);

        Map<String, Object> properties = Map.of(
                "deviceId", deviceId,
                "points", points.size());

        return TrackGeoJson.lineString(coordinates, properties);
    }

    private Position toEntity(Device device, PositionRequest request) {
        Position position = new Position(device, request.lat(), request.lon());
        position.setRecordedAt(request.recordedAt());
        position.setSpeed(request.speed());
        position.setAccuracy(request.accuracy());
        position.setAltitude(request.altitude());
        position.setBearing(request.bearing());
        position.setBattery(request.battery());
        return position;
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return MAX_HISTORY_LIMIT;
        }
        return Math.min(limit, MAX_HISTORY_LIMIT);
    }
}
