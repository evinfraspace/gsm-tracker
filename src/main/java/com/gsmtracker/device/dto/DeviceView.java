package com.gsmtracker.device.dto;

import com.gsmtracker.device.Device;

public record DeviceView(Long id, String name) {

    public static DeviceView from(Device device) {
        return new DeviceView(device.getId(), device.getName());
    }
}
