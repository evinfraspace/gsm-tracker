package com.gsmtracker.position;

import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceService;
import com.gsmtracker.position.dto.PositionRequest;
import com.gsmtracker.position.dto.PositionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private PositionService positionService;

    private final Device device = new Device("Test", "token");

    private PositionRequest sampleRequest() {
        return new PositionRequest(Instant.parse("2026-07-21T10:00:00Z"),
                55.75, 37.61, null, null, null, null, null);
    }

    @Test
    void storesNewPosition() {
        PositionRequest request = sampleRequest();
        when(positionRepository.existsByDeviceAndRecordedAt(device, request.recordedAt())).thenReturn(false);
        when(positionRepository.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        PositionResponse response = positionService.ingestSingle(device, request);

        assertThat(response.status()).isEqualTo("stored");
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    void skipsDuplicatePosition() {
        PositionRequest request = sampleRequest();
        when(positionRepository.existsByDeviceAndRecordedAt(device, request.recordedAt())).thenReturn(true);

        PositionResponse response = positionService.ingestSingle(device, request);

        assertThat(response.status()).isEqualTo("duplicate");
        verify(positionRepository, never()).save(any(Position.class));
    }
}
