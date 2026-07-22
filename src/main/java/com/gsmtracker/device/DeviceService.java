package com.gsmtracker.device;

import com.gsmtracker.common.DeviceNotFoundException;
import com.gsmtracker.device.dto.DeviceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceView> listDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Device getByIdOrThrow(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}
