package com.gsmtracker.position;

import java.util.List;

/** Расстояния на сфере (формула гаверсинуса). PostGIS не требуется. */
public final class GeoDistance {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private GeoDistance() {
    }

    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    public static double haversineMeters(Position a, Position b) {
        return haversineMeters(a.getLat(), a.getLon(), b.getLat(), b.getLon());
    }

    /** Длина ломанной по последовательности точек. */
    public static double pathLengthMeters(List<Position> points) {
        double total = 0.0;
        for (int i = 1; i < points.size(); i++) {
            total += haversineMeters(points.get(i - 1), points.get(i));
        }
        return total;
    }
}