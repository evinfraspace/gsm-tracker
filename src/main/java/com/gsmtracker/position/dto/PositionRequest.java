package com.gsmtracker.position.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PositionRequest(
        @NotNull Instant recordedAt,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lon,
        Double speed,
        Double accuracy,
        Double altitude,
        Double bearing,
        Integer battery
) {
}
