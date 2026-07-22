package com.gsmtracker.position.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PositionBatchRequest(
        @NotEmpty @Valid List<PositionRequest> points
) {
}
