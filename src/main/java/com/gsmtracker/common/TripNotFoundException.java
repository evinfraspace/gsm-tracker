package com.gsmtracker.common;

public class TripNotFoundException extends RuntimeException {

    public TripNotFoundException(Long deviceId, int index) {
        super("Trip not found: device=" + deviceId + ", index=" + index);
    }
}