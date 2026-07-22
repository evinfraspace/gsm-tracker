package com.gsmtracker.position.dto;

import java.util.Map;

/**
 * Minimal GeoJSON Feature with a LineString geometry, ready for Leaflet.
 * Note: GeoJSON coordinate order is [lon, lat].
 */
public record TrackGeoJson(String type, Geometry geometry, Map<String, Object> properties) {

    public record Geometry(String type, double[][] coordinates) {
    }

    public static TrackGeoJson lineString(double[][] coordinates, Map<String, Object> properties) {
        return new TrackGeoJson("Feature", new Geometry("LineString", coordinates), properties);
    }
}
