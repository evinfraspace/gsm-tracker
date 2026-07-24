package com.gsmtracker.position;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoDistanceTest {

    @Test
    void oneDegreeOfLatitudeIsAbout111km() {
        double meters = GeoDistance.haversineMeters(0.0, 0.0, 1.0, 0.0);
        assertThat(meters).isCloseTo(111_195.0, within(200.0));
    }

    @Test
    void samePointIsZero() {
        assertThat(GeoDistance.haversineMeters(55.75, 37.61, 55.75, 37.61)).isZero();
    }

    @Test
    void knownShortDistance() {
        double meters = GeoDistance.haversineMeters(55.7500, 37.6100, 55.7625, 37.6300);
        assertThat(meters).isCloseTo(1870.0, within(100.0));
    }
}