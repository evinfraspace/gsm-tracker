package com.gsmtracker.common;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(Long id) {
        super("Device not found: " + id);
    }
}
