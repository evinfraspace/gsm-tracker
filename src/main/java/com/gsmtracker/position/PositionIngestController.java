package com.gsmtracker.position;

import com.gsmtracker.auth.DeviceTokenFilter;
import com.gsmtracker.device.Device;
import com.gsmtracker.position.dto.BatchResponse;
import com.gsmtracker.position.dto.PositionBatchRequest;
import com.gsmtracker.position.dto.PositionRequest;
import com.gsmtracker.position.dto.PositionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/positions")
public class PositionIngestController {

    private final PositionService positionService;

    public PositionIngestController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponse ingest(
            @RequestAttribute(DeviceTokenFilter.DEVICE_ATTRIBUTE) Device device,
            @Valid @RequestBody PositionRequest request) {
        return positionService.ingestSingle(device, request);
    }

    @PostMapping("/batch")
    public BatchResponse ingestBatch(
            @RequestAttribute(DeviceTokenFilter.DEVICE_ATTRIBUTE) Device device,
            @Valid @RequestBody PositionBatchRequest request) {
        return positionService.ingestBatch(device, request.points());
    }
}
